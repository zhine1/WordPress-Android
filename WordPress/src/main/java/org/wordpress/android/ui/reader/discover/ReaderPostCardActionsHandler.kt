package org.wordpress.android.ui.reader.discover

import android.content.ActivityNotFoundException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.wordpress.android.analytics.AnalyticsTracker.Stat.READER_ARTICLE_VISITED
import org.wordpress.android.analytics.AnalyticsTracker.Stat.SHARED_ITEM_READER
import org.wordpress.android.models.ReaderPost
import org.wordpress.android.ui.reader.discover.PrimaryReaderPostCardActionType.Bookmark
import org.wordpress.android.ui.reader.discover.PrimaryReaderPostCardActionType.Comments
import org.wordpress.android.ui.reader.discover.PrimaryReaderPostCardActionType.Like
import org.wordpress.android.ui.reader.discover.PrimaryReaderPostCardActionType.Reblog
import org.wordpress.android.ui.reader.discover.ReaderNavigationEvents.OpenPost
import org.wordpress.android.ui.reader.discover.ReaderNavigationEvents.SharePost
import org.wordpress.android.ui.reader.discover.ReaderNavigationEvents.ShowReaderComments
import org.wordpress.android.ui.reader.discover.SecondaryReaderPostCardActionType.BlockSite
import org.wordpress.android.ui.reader.discover.SecondaryReaderPostCardActionType.Follow
import org.wordpress.android.ui.reader.discover.SecondaryReaderPostCardActionType.Share
import org.wordpress.android.ui.reader.discover.SecondaryReaderPostCardActionType.SiteNotifications
import org.wordpress.android.ui.reader.discover.SecondaryReaderPostCardActionType.VisitSite
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.analytics.AnalyticsTrackerWrapper
import org.wordpress.android.viewmodel.Event
import javax.inject.Inject

// TODO malinjir start using this class in legacy ReaderPostAdapter and ReaderPostListFragment
class ReaderPostCardActionsHandler @Inject constructor(
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper
) {
    private val _navigationEvents = MutableLiveData<Event<ReaderNavigationEvents>>()
    val navigationEvents: LiveData<Event<ReaderNavigationEvents>> = _navigationEvents

    fun onAction(post: ReaderPost, type: ReaderPostCardActionType) {
        when (type) {
            Follow -> handleFollowClicked(post)
            SiteNotifications -> handleSiteNotificationsClicked(post.postId, post.blogId)
            Share -> handleShareClicked(post)
            VisitSite -> handleVisitSiteClicked(post)
            BlockSite -> handleBlockSiteClicked(post.postId, post.blogId)
            Like -> handleLikeClicked(post.postId, post.blogId)
            Bookmark -> handleBookmarkClicked(post.postId, post.blogId)
            Reblog -> handleReblogClicked(post.postId, post.blogId)
            Comments -> handleCommentsClicked(post.postId, post.blogId)
        }
    }

    private fun handleFollowClicked(post: ReaderPost) {
        AppLog.d(AppLog.T.READER, "Follow not implemented")
    }

    private fun handleSiteNotificationsClicked(postId: Long, blogId: Long) {
        AppLog.d(AppLog.T.READER, "SiteNotifications not implemented")
    }

    private fun handleShareClicked(post: ReaderPost) {
        analyticsTrackerWrapper.track(SHARED_ITEM_READER, post.blogId)
        try {
            _navigationEvents.postValue(Event(SharePost(post)))
        } catch (ex: ActivityNotFoundException) {
            // TODO malinjir show toast - R.string.reader_toast_err_share_intent
        }
    }

    private fun handleVisitSiteClicked(post: ReaderPost) {
        analyticsTrackerWrapper.track(READER_ARTICLE_VISITED)
        _navigationEvents.postValue(Event(OpenPost(post)))
    }

    private fun handleBlockSiteClicked(postId: Long, blogId: Long) {
        AppLog.d(AppLog.T.READER, "Block site not implemented")
    }

    private fun handleLikeClicked(postId: Long, blogId: Long) {
        AppLog.d(AppLog.T.READER, "Like not implemented")
    }

    private fun handleBookmarkClicked(postId: Long, blogId: Long) {
        AppLog.d(AppLog.T.READER, "Bookmark not implemented")
    }

    private fun handleReblogClicked(postId: Long, blogId: Long) {
        AppLog.d(AppLog.T.READER, "Reblog not implemented")
    }

    private fun handleCommentsClicked(postId: Long, blogId: Long) {
        _navigationEvents.postValue(Event(ShowReaderComments(blogId, postId)))
    }
}
