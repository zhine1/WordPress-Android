package org.wordpress.android.ui.mysite

import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.wordpress.android.R
import org.wordpress.android.analytics.AnalyticsTracker.Stat
import org.wordpress.android.analytics.AnalyticsTracker.Stat.QUICK_START_TASK_DIALOG_NEGATIVE_TAPPED
import org.wordpress.android.analytics.AnalyticsTracker.Stat.QUICK_START_TASK_DIALOG_VIEWED
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.generated.SiteActionBuilder
import org.wordpress.android.fluxc.model.DynamicCardType
import org.wordpress.android.fluxc.model.DynamicCardType.CUSTOMIZE_QUICK_START
import org.wordpress.android.fluxc.model.DynamicCardType.GROW_QUICK_START
import org.wordpress.android.fluxc.model.SiteHomepageSettings.ShowOnFront
import org.wordpress.android.fluxc.store.DynamicCardStore
import org.wordpress.android.fluxc.store.QuickStartStore
import org.wordpress.android.fluxc.store.QuickStartStore.QuickStartTask
import org.wordpress.android.fluxc.store.QuickStartStore.QuickStartTask.CREATE_SITE
import org.wordpress.android.fluxc.store.QuickStartStore.QuickStartTask.EDIT_HOMEPAGE
import org.wordpress.android.fluxc.store.QuickStartStore.QuickStartTask.UPDATE_SITE_TITLE
import org.wordpress.android.fluxc.store.QuickStartStore.QuickStartTaskType
import org.wordpress.android.fluxc.store.QuickStartStore.QuickStartTaskType.CUSTOMIZE
import org.wordpress.android.fluxc.store.QuickStartStore.QuickStartTaskType.GROW
import org.wordpress.android.fluxc.store.QuickStartStore.QuickStartTaskType.UNKNOWN
import org.wordpress.android.fluxc.store.SiteStore.CompleteQuickStartPayload
import org.wordpress.android.fluxc.store.SiteStore.CompleteQuickStartVariant.NEXT_STEPS
import org.wordpress.android.modules.BG_THREAD
import org.wordpress.android.ui.mysite.MySiteUiState.PartialState.QuickStartUpdate
import org.wordpress.android.ui.mysite.QuickStartRepository.QuickStartReminderAction.CancelReminder
import org.wordpress.android.ui.pages.SnackbarMessageHolder
import org.wordpress.android.ui.prefs.AppPrefs
import org.wordpress.android.ui.quickstart.QuickStartEvent
import org.wordpress.android.ui.quickstart.QuickStartMySitePrompts
import org.wordpress.android.ui.quickstart.QuickStartNoticeDetails
import org.wordpress.android.ui.quickstart.QuickStartTaskDetails
import org.wordpress.android.ui.utils.UiString.UiStringText
import org.wordpress.android.util.EventBusWrapper
import org.wordpress.android.util.HtmlCompatWrapper
import org.wordpress.android.util.QuickStartUtils
import org.wordpress.android.util.QuickStartUtilsWrapper
import org.wordpress.android.util.SiteUtils
import org.wordpress.android.util.analytics.AnalyticsTrackerWrapper
import org.wordpress.android.util.config.MySiteImprovementsFeatureConfig
import org.wordpress.android.util.mapAsync
import org.wordpress.android.util.merge
import org.wordpress.android.viewmodel.Event
import org.wordpress.android.viewmodel.ResourceProvider
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class QuickStartRepository
@Inject constructor(
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher,
    private val quickStartStore: QuickStartStore,
    private val quickStartUtils: QuickStartUtilsWrapper,
    private val selectedSiteRepository: SelectedSiteRepository,
    private val resourceProvider: ResourceProvider,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper,
    private val dispatcher: Dispatcher,
    private val eventBus: EventBusWrapper,
    private val dynamicCardStore: DynamicCardStore,
    private val htmlCompat: HtmlCompatWrapper,
    private val mySiteImprovementsFeatureConfig: MySiteImprovementsFeatureConfig
) : CoroutineScope, MySiteSource<QuickStartUpdate> {
    private val job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = bgDispatcher + job

    private val detailsMap: Map<QuickStartTask, QuickStartTaskDetails> = QuickStartTaskDetails.values()
            .associateBy { it.task }
    private val refresh = MutableLiveData<Boolean>()
    private val _activeTask = MutableLiveData<QuickStartTask?>()
    private val _onSnackbar = MutableLiveData<Event<SnackbarMessageHolder>>()
    private val _onQuickStartSnackbar = MutableLiveData<Event<QuickStartSnackbar>>()
    private val _onReminderAction = MutableLiveData<Event<QuickStartReminderAction>>()
    private val _onQuickStartMySitePrompts = MutableLiveData<Event<QuickStartMySitePrompts>>()
    val onSnackbar = _onSnackbar as LiveData<Event<SnackbarMessageHolder>>
    val onQuickStartSnackbar = _onQuickStartSnackbar as LiveData<Event<QuickStartSnackbar>>
    val onQuickStartReminderAction = _onReminderAction as LiveData<Event<QuickStartReminderAction>>
    val onQuickStartMySitePrompts = _onQuickStartMySitePrompts as LiveData<Event<QuickStartMySitePrompts>>
    val activeTask = _activeTask as LiveData<QuickStartTask?>

    private var pendingTask: QuickStartTask? = null

    private fun buildQuickStartCategory(siteId: Int, quickStartTaskType: QuickStartTaskType) = QuickStartCategory(
            quickStartTaskType,
            uncompletedTasks = quickStartStore.getUncompletedTasksByType(siteId.toLong(), quickStartTaskType)
                    .mapNotNull { detailsMap[it] },
            completedTasks = quickStartStore.getCompletedTasksByType(siteId.toLong(), quickStartTaskType)
                    .mapNotNull { detailsMap[it] })

    override fun buildSource(coroutineScope: CoroutineScope, siteId: Int): LiveData<QuickStartUpdate> {
        _activeTask.value = null
        pendingTask = null
        if (selectedSiteRepository.getSelectedSite()?.showOnFront == ShowOnFront.POSTS.value &&
                !quickStartStore.hasDoneTask(siteId.toLong(), EDIT_HOMEPAGE)) {
            setTaskDoneAndTrack(EDIT_HOMEPAGE, siteId)
            refresh()
        }
        val quickStartTaskTypes = refresh.mapAsync(coroutineScope) {
            dynamicCardStore.getCards(siteId).dynamicCardTypes.map { it.toQuickStartTaskType() }.onEach { taskType ->
                if (quickStartUtils.isEveryQuickStartTaskDoneForType(siteId, taskType)) {
                    onCategoryCompleted(siteId, taskType)
                }
            }
        }
        return merge(quickStartTaskTypes, activeTask) { types, activeTask ->
            val categories = if (quickStartUtils.isQuickStartInProgress(siteId)) {
                types?.map { buildQuickStartCategory(siteId, it) } ?: listOf()
            } else {
                listOf()
            }
            QuickStartUpdate(activeTask, categories)
        }
    }

    fun startQuickStart(newSiteLocalID: Int) {
        if (newSiteLocalID != -1) {
            quickStartUtils.startQuickStart(newSiteLocalID)
            refresh()
        }
    }

    fun refresh() {
        refresh.postValue(true)
        showQuickStartNoticeIfNecessary()
    }

    fun setActiveTask(task: QuickStartTask) {
        _activeTask.postValue(task)
        pendingTask = null
        if (task == UPDATE_SITE_TITLE) {
            val shortQuickStartMessage = resourceProvider.getString(
                    R.string.quick_start_dialog_update_site_title_message_short,
                    SiteUtils.getSiteNameOrHomeURL(selectedSiteRepository.getSelectedSite())
            )
            _onSnackbar.postValue(Event(SnackbarMessageHolder(UiStringText(shortQuickStartMessage.asHtml()))))
        } else {
            QuickStartMySitePrompts.getPromptDetailsForTask(task)?.let { activeTutorialPrompt ->
                _onQuickStartMySitePrompts.postValue(Event(activeTutorialPrompt))
            }
        }
    }

    fun clearActiveTask() {
        _activeTask.value = null
    }

    @JvmOverloads fun completeTask(
        task: QuickStartTask,
        refreshImmediately: Boolean = false,
        quickStartEvent: QuickStartEvent? = null
    ) {
        selectedSiteRepository.getSelectedSite()?.let { site ->
            if (task != activeTask.value && task != pendingTask) return
            _activeTask.value = null
            pendingTask = null
            if (quickStartStore.hasDoneTask(site.id.toLong(), task)) return
            _onReminderAction.value = Event(CancelReminder)
            // If we want notice and reminders, we should call QuickStartUtils.completeTaskAndRemindNextOne here
            setTaskDoneAndTrack(task, site.id)
            // We need to refresh immediately. This is useful for tasks that are completed on the My Site screen.
            if (refreshImmediately) {
                refresh()
            }
            if (quickStartUtils.isEveryQuickStartTaskDone(site.id)) {
                quickStartStore.setQuickStartCompleted(site.id.toLong(), true)
                analyticsTrackerWrapper.track(Stat.QUICK_START_ALL_TASKS_COMPLETED, mySiteImprovementsFeatureConfig)
                val payload = CompleteQuickStartPayload(site, NEXT_STEPS.toString())
                dispatcher.dispatch(SiteActionBuilder.newCompleteQuickStartAction(payload))
            }  else if (quickStartEvent?.task == task) {
                AppPrefs.setQuickStartNoticeRequired(true)
            } else if (quickStartStore.hasDoneTask(site.id.toLong(), CREATE_SITE)) {
                val nextTask =
                        QuickStartUtils.getNextUncompletedQuickStartTaskForReminderNotification(
                                quickStartStore,
                                site.id.toLong(),
                                task.taskType
                        )
                if (nextTask != null) {
                    _onReminderAction.value = Event(QuickStartReminderAction.SetReminder(nextTask))
                }
            }
        }
    }

    private fun setTaskDoneAndTrack(
        task: QuickStartTask,
        siteId: Int
    ) {
        AppPrefs.setQuickStartNoticeRequired(true)
        quickStartStore.setDoneTask(siteId.toLong(), task, true)
        analyticsTrackerWrapper.track(quickStartUtils.getTaskCompletedTracker(task), mySiteImprovementsFeatureConfig)
    }

    fun requestNextStepOfTask(task: QuickStartTask) {
        if (task != activeTask.value) return
        _activeTask.value = null
        pendingTask = task
        eventBus.postSticky(QuickStartEvent(task))
    }

    fun clear() {
        job.cancel()
    }

    private suspend fun onCategoryCompleted(siteId: Int, categoryType: QuickStartTaskType) {
        val completionMessage = getCategoryCompletionMessage(categoryType)
        _onSnackbar.postValue(Event(SnackbarMessageHolder(UiStringText(completionMessage.asHtml()))))
        dynamicCardStore.removeCard(siteId, categoryType.toDynamicCardType())
    }

    private fun getCategoryCompletionMessage(taskType: QuickStartTaskType) = when (taskType) {
        CUSTOMIZE -> R.string.quick_start_completed_type_customize_message
        GROW -> R.string.quick_start_completed_type_grow_message
        UNKNOWN -> throw IllegalArgumentException("Unexpected quick start type")
    }.let { resourceProvider.getString(it) }

    private fun String.asHtml() = htmlCompat.fromHtml(this)

    private fun DynamicCardType.toQuickStartTaskType(): QuickStartTaskType {
        return when (this) {
            CUSTOMIZE_QUICK_START -> CUSTOMIZE
            GROW_QUICK_START -> GROW
        }
    }

    private fun QuickStartTaskType.toDynamicCardType(): DynamicCardType {
        return when (this) {
            CUSTOMIZE -> CUSTOMIZE_QUICK_START
            GROW -> GROW_QUICK_START
            UNKNOWN -> throw IllegalArgumentException("Unexpected quick start type")
        }
    }

    private fun showQuickStartNoticeIfNecessary() {
        selectedSiteRepository.getSelectedSite()?.let { site ->
            if (!quickStartUtils.isQuickStartInProgress(site.id)
                    || !AppPrefs.isQuickStartNoticeRequired()
            ) {
                return
            }
            val taskToPrompt = QuickStartUtils.getNextUncompletedQuickStartTask(
                    quickStartStore,
                    site.id.toLong()
            ) // CUSTOMIZE is default type
            if (taskToPrompt != null) {
                val noticeDetails = QuickStartNoticeDetails.getNoticeForTask(taskToPrompt) ?: return
                analyticsTrackerWrapper.track(QUICK_START_TASK_DIALOG_VIEWED)

                _onQuickStartSnackbar.postValue(Event(QuickStartSnackbar(
                        title = noticeDetails.titleResId,
                        message = noticeDetails.messageResId,
                        durationResource = R.integer.quick_start_snackbar_duration_ms,
                        positiveButton = R.string.quick_start_button_positive,
                        negativeButton = R.string.quick_start_button_negative,
                        quickStartTask = taskToPrompt,
                        positiveAction = {
                            analyticsTrackerWrapper.track(Stat.QUICK_START_TASK_DIALOG_POSITIVE_TAPPED)
                            setActiveTask(it)
                        },
                        negativeAction = {
                            AppPrefs.setLastSkippedQuickStartTask(taskToPrompt)
                            analyticsTrackerWrapper.track(
                                    QUICK_START_TASK_DIALOG_NEGATIVE_TAPPED
                            )
                        }
                )))
            }
        }
    }

    fun onQuickStartSnackbarShown() {
        AppPrefs.setQuickStartNoticeRequired(false)
    }

    data class QuickStartCategory(
        val taskType: QuickStartTaskType,
        val uncompletedTasks: List<QuickStartTaskDetails>,
        val completedTasks: List<QuickStartTaskDetails>
    )

    data class QuickStartSnackbar(
        @StringRes val title: Int,
        @StringRes val message: Int,
        @IntegerRes val durationResource: Int,
        @StringRes val positiveButton: Int,
        @StringRes val negativeButton: Int,
        val quickStartTask: QuickStartTask,
        val positiveAction: (QuickStartTask) -> Unit,
        val negativeAction: (QuickStartTask) -> Unit
    )

    sealed class QuickStartReminderAction {
        object CancelReminder : QuickStartReminderAction()
        data class SetReminder(val quickStartTask: QuickStartTask) : QuickStartReminderAction()

    }
}
