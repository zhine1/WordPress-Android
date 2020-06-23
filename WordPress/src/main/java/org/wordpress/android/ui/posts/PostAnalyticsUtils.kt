package org.wordpress.android.ui.posts

import org.wordpress.android.analytics.AnalyticsTracker.Stat
import org.wordpress.android.fluxc.model.post.PostStatus
import org.wordpress.android.ui.posts.prepublishing.visibility.PrepublishingVisibilityItemUiState.Visibility
import org.wordpress.android.util.DateTimeUtils
import org.wordpress.android.util.analytics.AnalyticsTrackerWrapper
import java.lang.IllegalStateException
import java.util.Date

private const val VIA = "via"
private const val VISIBILITY = "visibility"
private const val ERA = "era"
private const val FUTURE_ERA = "future"
private const val PRESENT_ERA = "present"
private const val PAST_ERA = "past"
const val POST_SETTINGS = "settings"
const val PREPUBLISHING_NUDGES = "prepublishing_nudges"

fun AnalyticsTrackerWrapper.trackPrepublishingNudges(stat: Stat) {
    this.track(stat, mapOf(VIA to PREPUBLISHING_NUDGES))
}

fun AnalyticsTrackerWrapper.trackPostSettings(stat: Stat) {
    this.track(stat, mapOf(VIA to POST_SETTINGS))
}

fun AnalyticsTrackerWrapper.trackPublishSchedule(via: String, date: Date) {
    val timeEra = when {
        PostUtils.isPublishDateInTheFuture(DateTimeUtils.iso8601FromDate(date)) -> FUTURE_ERA
        PostUtils.isPublishDateInThePast(DateTimeUtils.iso8601FromDate(date), Date()) -> PAST_ERA
        else -> PRESENT_ERA
    }

    this.track(Stat.EDITOR_POST_SCHEDULE_CHANGED, mapOf(VIA to via, ERA to timeEra))
}

fun AnalyticsTrackerWrapper.trackPrepublishingVisibility(visibility: Visibility) {
    this.track(
            Stat.EDITOR_POST_VISIBILITY_CHANGED,
            mapOf(VIA to PREPUBLISHING_NUDGES, VISIBILITY to visibility.toString())
    )
}

fun AnalyticsTrackerWrapper.trackPrepublishingVisibility(postStatus: PostStatus) {
    this.track(
            Stat.EDITOR_POST_VISIBILITY_CHANGED,
            mapOf(VIA to PREPUBLISHING_NUDGES, VISIBILITY to postStatus.toString())
    )
}


