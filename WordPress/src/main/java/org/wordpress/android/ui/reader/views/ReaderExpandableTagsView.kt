package org.wordpress.android.ui.reader.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver.OnPreDrawListener
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.wordpress.android.R
import org.wordpress.android.WordPress
import org.wordpress.android.ui.reader.discover.interests.TagUiState
import org.wordpress.android.ui.reader.views.presenters.ExpandableTagsViewPresenter
import org.wordpress.android.ui.reader.views.presenters.ExpandableTagsViewPresenter.IExpandableTagsView
import org.wordpress.android.ui.utils.UiHelpers
import javax.inject.Inject

class ReaderExpandableTagsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ChipGroup(context, attrs, defStyleAttr), IExpandableTagsView {
    @Inject lateinit var uiHelpers: UiHelpers
    @Inject lateinit var expandableTagsViewPresenterFactory: ExpandableTagsViewPresenter.Factory
    private lateinit var expandableTagsViewPresenter: ExpandableTagsViewPresenter

    private val tagChips
        get() = (0 until expandableTagsViewPresenter.tagChipsCount).map { getChildAt(it) as Chip }

    init {
        (context.applicationContext as WordPress).component().inject(this)
        layoutDirection = View.LAYOUT_DIRECTION_LOCALE
    }

    fun onBind(tags: List<TagUiState>) {
        expandableTagsViewPresenter = expandableTagsViewPresenterFactory.create()
        updateTagsUi(tags)
        expandableTagsViewPresenter.onBind(tags, this)
    }

    fun onUnBind() {
        expandableTagsViewPresenter.onUnbind()
    }

    private fun updateTagsUi(tags: List<TagUiState>) {
        removeAllViews()
        addOverflowIndicatorChip()
        addTagChips(tags)
    }

    private fun addOverflowIndicatorChip() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val chip = inflater.inflate(R.layout.reader_expandable_tags_view_overflow_chip, this, false) as Chip
        chip.setOnCheckedChangeListener { _, isChecked -> expandableTagsViewPresenter.expandLayout(isChecked) }
        addView(chip)
    }

    private fun addTagChips(tags: List<TagUiState>) {
        tags.forEachIndexed { index, tag ->
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val chip = inflater.inflate(R.layout.reader_expandable_tags_view_chip, this, false) as Chip
            chip.tag = tag.slug
            chip.text = tag.title
            chip.maxWidth = tag.maxWidth
            chip.setOnClickListener { // TODO - set click listener
            }
            addView(chip, index)
        }
    }

    override fun updateTagsVisibility(tagUiStates: List<TagUiState>) {
        tagChips.forEachIndexed { index, chip ->
            uiHelpers.updateVisibility(chip, tagUiStates[index].visible)
        }
    }

    override fun updateOverflowIndicatorVisibility(isVisible: Boolean) {
        val overflowIndicatorChip = getChildAt(expandableTagsViewPresenter.overflowChipIndex) as Chip
        uiHelpers.updateVisibility(overflowIndicatorChip, isVisible)
    }

    override fun refreshLayout() {
        requestLayout()
    }

    override fun collapseToSingleLine(singleLine: Boolean) {
        isSingleLine = singleLine
    }

    override fun isChipWithinBounds(chipIndex: Int): Boolean {
        val chip = getChildAt(chipIndex) ?: return false
        return if (isSingleLine) {
            if (layoutDirection == View.LAYOUT_DIRECTION_LTR) {
                chip.right <= right - (paddingEnd + chipSpacingHorizontal)
            } else {
                chip.left >= left + (paddingStart + chipSpacingHorizontal)
            }
        } else {
            chip.bottom <= bottom - (paddingBottom + chipSpacingVertical)
        }
    }

    override fun onPreLayout(what: () -> Unit) {
        viewTreeObserver.addOnPreDrawListener(object : OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                viewTreeObserver.removeOnPreDrawListener(this)
                what.invoke()
                return true
            }
        })
    }
}
