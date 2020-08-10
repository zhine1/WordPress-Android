package org.wordpress.android.ui.reader.views.presenters

import org.wordpress.android.ui.reader.discover.interests.TagUiState
import javax.inject.Inject

class ExpandableTagsViewPresenter {
    private lateinit var tagUiStates: List<TagUiState>
    private var expandableTagsView: IExpandableTagsView? = null
    private var shouldCollapseToSingleLine: Boolean = true

    interface IExpandableTagsView {
        fun updateTagsVisibility(tagUiStates: List<TagUiState>)
        fun updateOverflowIndicatorVisibility(isVisible: Boolean)
        fun isChipWithinBounds(chipIndex: Int): Boolean
        fun collapseToSingleLine(singleLine: Boolean)
        fun onPreLayout(what: () -> Unit)
        fun refreshLayout()
    }

    val tagChipsCount
        get() = tagUiStates.size

    val lastVisibleTagChipIndex
        get() = tagUiStates.filter { it.visible }.lastIndex

    val hiddenTagChipsCount
        get() = tagChipsCount - (lastVisibleTagChipIndex + 1)

    val overflowChipIndex
        get() = tagUiStates.size

    val isOverflowIndicatorChipOutsideBounds
        get() = !(expandableTagsView?.isChipWithinBounds(overflowChipIndex) ?: true)

    fun onBind(tagUiStates: List<TagUiState>, expandableTagsView: IExpandableTagsView) {
        this.expandableTagsView = expandableTagsView
        this.tagUiStates = tagUiStates
        expandLayout(false)
    }

    fun expandLayout(isChecked: Boolean) {
        shouldCollapseToSingleLine = !isChecked
        expandableTagsView?.collapseToSingleLine(shouldCollapseToSingleLine)
        showAllChips()
        expandableTagsView?.onPreLayout {
            hideTagChipsOutsideBounds()
            updateLastVisibleTagChip()
            updateOverflowIndicatorChip()
        }
        expandableTagsView?.refreshLayout()
    }

    private fun showAllChips() {
        tagUiStates = tagUiStates.mapIndexed { index, uiState ->
            createTagUiState(true, index)
        }
        expandableTagsView?.updateTagsVisibility(tagUiStates)
    }

    private fun hideTagChipsOutsideBounds() {
        expandableTagsView?.let {
            tagUiStates = tagUiStates.mapIndexed { index, uiState ->
                createTagUiState(it.isChipWithinBounds(index), index)
            }
            it.updateTagsVisibility(tagUiStates)
        }
    }

    private fun updateLastVisibleTagChip() {
        tagUiStates = createTagUiStates(
                createTagUiState(!isOverflowIndicatorChipOutsideBounds, lastVisibleTagChipIndex),
                lastVisibleTagChipIndex
        )
        expandableTagsView?.updateTagsVisibility(tagUiStates)
    }
    private fun updateOverflowIndicatorChip() {
        val showOverflowIndicatorChip = hiddenTagChipsCount > 0 || !shouldCollapseToSingleLine
        expandableTagsView?.updateOverflowIndicatorVisibility(showOverflowIndicatorChip)
    }

    private fun createTagUiStates(
        tagUiState: TagUiState,
        index: Int
    ): List<TagUiState> {
        return tagUiStates.mapIndexed { i, uiState ->
            if (i == index) {
                tagUiState
            } else {
                createTagUiState(uiState.visible, i)
            }
        }
    }

    private fun createTagUiState(
        visibility: Boolean = false,
        index: Int
    ): TagUiState {
        return tagUiStates[index].copy(visible = visibility) }

    fun onUnbind() {
        expandableTagsView = null
    }

    class Factory
    @Inject constructor() {
        fun create(): ExpandableTagsViewPresenter {
            return ExpandableTagsViewPresenter()
        }
    }
}
