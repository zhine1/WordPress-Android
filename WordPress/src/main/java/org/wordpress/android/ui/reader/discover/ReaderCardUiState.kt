package org.wordpress.android.ui.reader.discover

import android.text.Spanned
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import org.wordpress.android.ui.reader.discover.ReaderPostCardAction.PrimaryAction
import org.wordpress.android.ui.reader.discover.ReaderPostCardAction.SecondaryAction
import org.wordpress.android.ui.reader.models.ReaderImageList
import org.wordpress.android.ui.utils.UiDimen
import org.wordpress.android.ui.utils.UiString
import org.wordpress.android.util.image.ImageType

sealed class ReaderCardUiState {
    data class ReaderPostUiState(
        val postId: Long,
        val blogId: Long,
        val dateLine: String,
        val title: String?,
        val blogName: String?,
        val excerpt: String?, // mTxtText
        val blogUrl: String?,
        val photoTitle: String?,
        val featuredImageUrl: String?,
        val featuredImageCornerRadius: UiDimen,
        val fullVideoUrl: String?,
        val avatarOrBlavatarUrl: String?,
        val thumbnailStripSection: GalleryThumbnailStripData?,
        val discoverSection: DiscoverLayoutUiState?,
        val videoOverlayVisibility: Boolean,
        val moreMenuVisibility: Boolean,
        val photoFrameVisibility: Boolean,
        val bookmarkAction: PrimaryAction,
        val likeAction: PrimaryAction,
        val reblogAction: PrimaryAction,
        val commentsAction: PrimaryAction,
        val moreMenuItems: List<SecondaryAction>,
        val postHeaderClickData: PostHeaderClickData?,
        val onItemClicked: (Long, Long) -> Unit,
        val onItemRendered: (Long, Long) -> Unit,
        val onMoreButtonClicked: (Long, Long, View) -> Unit,
        val onVideoOverlayClicked: (Long, Long) -> Unit
    ) : ReaderCardUiState() {
        val dotSeparatorVisibility: Boolean = blogUrl != null

        data class PostHeaderClickData(
            val onPostHeaderViewClicked: ((Long, Long) -> Unit)?,
            @AttrRes val background: Int
        )

        data class GalleryThumbnailStripData(
            val images: ReaderImageList,
            val isPrivate: Boolean,
            val content: String // needs to be here as it's required by ReaderThumbnailStrip
        )

        data class DiscoverLayoutUiState(
            val discoverText: Spanned,
            val discoverAvatarUrl: String,
            val imageType: ImageType,
            val onDiscoverClicked: ((Long, Long) -> Unit)
        )
    }
}

sealed class ReaderPostCardAction {
    abstract val type: ReaderPostCardActionType
    open val onClicked: ((Long, Long, ReaderPostCardActionType) -> Unit)? = null
    open val isSelected: Boolean = false

    data class PrimaryAction(
        val isEnabled: Boolean,
        val contentDescription: UiString? = null,
        val count: Int = 0,
        override val isSelected: Boolean = false,
        override val type: PrimaryReaderPostCardActionType,
        override val onClicked: ((Long, Long, ReaderPostCardActionType) -> Unit)? = null
    ) : ReaderPostCardAction()

    data class SecondaryAction(
        val label: UiString,
        @AttrRes val labelColor: Int,
        @DrawableRes val iconRes: Int,
        @AttrRes val iconColor: Int = labelColor,
        override val isSelected: Boolean = false,
        override val type: SecondaryReaderPostCardActionType,
        override val onClicked: (Long, Long, ReaderPostCardActionType) -> Unit
    ) : ReaderPostCardAction()
}

sealed class ReaderPostCardActionType {
    abstract val id: Int
}

sealed class PrimaryReaderPostCardActionType(override val id: Int) : ReaderPostCardActionType() {
    object Like : PrimaryReaderPostCardActionType(LIKE_ACTION_ID)
    object Bookmark : PrimaryReaderPostCardActionType(BOOKMARK_ACTION_ID)
    object Reblog : PrimaryReaderPostCardActionType(REBLOG_ACTION_ID)
    object Comments : PrimaryReaderPostCardActionType(COMMENTS_ACTION_ID)
}

sealed class SecondaryReaderPostCardActionType(override val id: Int) : ReaderPostCardActionType() {
    object Follow : SecondaryReaderPostCardActionType(FOLLOW_ACTION_ID)
    object SiteNotifications : SecondaryReaderPostCardActionType(SITE_NOTIFICATIONS_ACTION_ID)
    object Share : SecondaryReaderPostCardActionType(SHARE_ACTION_ID)
    object VisitSite : SecondaryReaderPostCardActionType(VISIT_SITE_ACTION_ID)
    object BlockSite : SecondaryReaderPostCardActionType(BLOCK_SITE_ACTION_ID)
}
const val LIKE_ACTION_ID = 1
const val BOOKMARK_ACTION_ID = 2
const val REBLOG_ACTION_ID = 3
const val COMMENTS_ACTION_ID = 4
const val FOLLOW_ACTION_ID = 5
const val SITE_NOTIFICATIONS_ACTION_ID = 6
const val SHARE_ACTION_ID = 7
const val VISIT_SITE_ACTION_ID = 8
const val BLOCK_SITE_ACTION_ID = 9
