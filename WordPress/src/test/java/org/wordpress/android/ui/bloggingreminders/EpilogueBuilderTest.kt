package org.wordpress.android.ui.bloggingreminders

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.wordpress.android.R.drawable
import org.wordpress.android.R.string
import org.wordpress.android.fluxc.model.BloggingRemindersModel
import org.wordpress.android.fluxc.model.BloggingRemindersModel.Day.FRIDAY
import org.wordpress.android.fluxc.model.BloggingRemindersModel.Day.MONDAY
import org.wordpress.android.fluxc.model.BloggingRemindersModel.Day.SATURDAY
import org.wordpress.android.fluxc.model.BloggingRemindersModel.Day.SUNDAY
import org.wordpress.android.fluxc.model.BloggingRemindersModel.Day.THURSDAY
import org.wordpress.android.fluxc.model.BloggingRemindersModel.Day.TUESDAY
import org.wordpress.android.fluxc.model.BloggingRemindersModel.Day.WEDNESDAY
import org.wordpress.android.ui.bloggingreminders.BloggingRemindersItem.EmphasizedText
import org.wordpress.android.ui.bloggingreminders.BloggingRemindersItem.HighEmphasisText
import org.wordpress.android.ui.bloggingreminders.BloggingRemindersItem.Illustration
import org.wordpress.android.ui.bloggingreminders.BloggingRemindersItem.Title
import org.wordpress.android.ui.bloggingreminders.BloggingRemindersViewModel.UiState.PrimaryButton
import org.wordpress.android.ui.utils.HtmlMessageUtils
import org.wordpress.android.ui.utils.ListItemInteraction
import org.wordpress.android.ui.utils.UiString.UiStringRes
import org.wordpress.android.ui.utils.UiString.UiStringText
import org.wordpress.android.util.ListFormatterUtils
import org.wordpress.android.util.LocaleManagerWrapper
import java.util.Locale

@RunWith(MockitoJUnitRunner::class)
class EpilogueBuilderTest {
    @Mock lateinit var dayLabelUtils: DayLabelUtils
    @Mock lateinit var localeManagerWrapper: LocaleManagerWrapper
    @Mock lateinit var listFormatterUtils: ListFormatterUtils
    @Mock lateinit var htmlMessageUtils: HtmlMessageUtils
    private lateinit var epilogueBuilder: EpilogueBuilder
    private var done = false

    private val onDone: () -> Unit = {
        done = true
    }

    @Before
    fun setUp() {
        epilogueBuilder = EpilogueBuilder(dayLabelUtils, localeManagerWrapper, listFormatterUtils, htmlMessageUtils)
        done = false
        whenever(localeManagerWrapper.getLocale()).thenReturn(Locale.US)
    }

    @Test
    fun `builds UI model with no selected days`() {
        val bloggingRemindersModel = BloggingRemindersModel(1, setOf())
        val uiModel = epilogueBuilder.buildUiItems(bloggingRemindersModel)

        assertModelWithNoSelection(uiModel)
    }

    @Test
    fun `builds UI model with selected days`() {
        val bloggingRemindersModel = BloggingRemindersModel(1, setOf(WEDNESDAY, SUNDAY))
        val dayLabel = "twice"
        whenever(dayLabelUtils.buildLowercaseNTimesLabel(bloggingRemindersModel))
                .thenReturn(dayLabel)
        val selectedDays = "<b>Wednesday</b>, <b>Sunday</b>"
        whenever(listFormatterUtils.formatList(listOf("<b>Wednesday</b>", "<b>Sunday</b>"))).thenReturn(selectedDays)
        val message = "You'll get reminders to blog <b>$dayLabel</b> a week on $selectedDays."
        whenever(
                htmlMessageUtils.getHtmlMessageFromStringFormatResId(
                        string.blogging_reminders_epilogue_body_days,
                        "<b>$dayLabel</b>",
                        selectedDays
                )
        ).thenReturn(message)

        val uiModel = epilogueBuilder.buildUiItems(bloggingRemindersModel)

        assertModelWithSelection(uiModel, message)
    }

    @Test
    fun `builds UI model with all days selected`() {
        val bloggingRemindersModel = BloggingRemindersModel(
                1,
                setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
        )
        val message = "You'll get reminders to blog <b>everyday</b>."
        whenever(
                htmlMessageUtils.getHtmlMessageFromStringFormatResId(
                        string.blogging_reminders_epilogue_body_everyday
                )
        ).thenReturn(
                message
        )
        val uiModel = epilogueBuilder.buildUiItems(bloggingRemindersModel)

        assertModelWithAllDaysSelection(uiModel, message)
    }

    @Test
    fun `builds primary button`() {
        val primaryButton = epilogueBuilder.buildPrimaryButton(onDone)

        assertThat(primaryButton).isEqualTo(
                PrimaryButton(
                        UiStringRes(string.blogging_reminders_done),
                        true,
                        ListItemInteraction.create(onDone)
                )
        )
    }

    @Test
    fun `click on primary button dismisses bottomsheet`() {
        val primaryButton = epilogueBuilder.buildPrimaryButton(onDone)

        primaryButton.onClick.click()

        assertThat(done).isTrue
    }

    private fun assertModelWithNoSelection(
        uiModel: List<BloggingRemindersItem>
    ) {
        assertThat(uiModel[0]).isEqualTo(Illustration(drawable.img_illustration_bell_yellow_96dp))
        assertThat(uiModel[1]).isEqualTo(Title(UiStringRes(string.blogging_reminders_epilogue_not_set_title)))
        assertThat(uiModel[2])
                .isEqualTo(
                        HighEmphasisText(
                                EmphasizedText(
                                        UiStringRes(string.blogging_reminders_epilogue_body_no_reminders),
                                        false
                                )
                        )
                )
    }

    private fun assertModelWithSelection(
        uiModel: List<BloggingRemindersItem>,
        message: String
    ) {
        assertThat(uiModel[0]).isEqualTo(Illustration(drawable.img_illustration_bell_yellow_96dp))
        assertThat(uiModel[1]).isEqualTo(Title(UiStringRes(string.blogging_reminders_epilogue_title)))
        assertThat(uiModel[2])
                .isEqualTo(
                        HighEmphasisText(
                                EmphasizedText(UiStringText(message), false)
                        )
                )
    }

    private fun assertModelWithAllDaysSelection(
        uiModel: List<BloggingRemindersItem>,
        message: String
    ) {
        assertThat(uiModel[0]).isEqualTo(Illustration(drawable.img_illustration_bell_yellow_96dp))
        assertThat(uiModel[1]).isEqualTo(Title(UiStringRes(string.blogging_reminders_epilogue_title)))
        assertThat(uiModel[2]).isEqualTo(
                HighEmphasisText(
                        EmphasizedText(UiStringText(message), false)
                )
        )
    }
}
