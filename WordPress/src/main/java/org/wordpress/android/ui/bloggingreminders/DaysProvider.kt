package org.wordpress.android.ui.bloggingreminders

import org.wordpress.android.fluxc.model.BloggingRemindersModel.Day
import org.wordpress.android.fluxc.model.BloggingRemindersModel.Day.FRIDAY
import org.wordpress.android.fluxc.model.BloggingRemindersModel.Day.MONDAY
import org.wordpress.android.fluxc.model.BloggingRemindersModel.Day.SATURDAY
import org.wordpress.android.fluxc.model.BloggingRemindersModel.Day.SUNDAY
import org.wordpress.android.fluxc.model.BloggingRemindersModel.Day.THURSDAY
import org.wordpress.android.fluxc.model.BloggingRemindersModel.Day.TUESDAY
import org.wordpress.android.fluxc.model.BloggingRemindersModel.Day.WEDNESDAY
import org.wordpress.android.ui.reader.utils.DateProvider
import java.util.Calendar
import javax.inject.Inject

class DaysProvider
@Inject constructor(private val dateProvider: DateProvider) {
    // TODO replace all the calendar references with DayOfWeek interface
    private val days = listOf(
            Calendar.SUNDAY to SUNDAY,
            Calendar.MONDAY to MONDAY,
            Calendar.TUESDAY to TUESDAY,
            Calendar.WEDNESDAY to WEDNESDAY,
            Calendar.THURSDAY to THURSDAY,
            Calendar.FRIDAY to FRIDAY,
            Calendar.SATURDAY to SATURDAY
    )

    fun getDays(): List<Pair<String, Day>> {
        val offset = dateProvider.getFirstDayOfTheWeek() - 1
        val shortWeekdays = dateProvider.getShortWeekdays()
        val orderedDays = days.takeLast(days.size - offset) + days.take(offset)
        return orderedDays.map { shortWeekdays[it.first] to it.second }
    }
}
