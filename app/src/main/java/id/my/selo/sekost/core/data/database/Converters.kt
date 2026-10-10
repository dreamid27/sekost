package id.my.selo.sekost.core.data.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.YearMonth

internal class Converters {
    @TypeConverter fun localDateToEpochDay(value: LocalDate?): Long? = value?.toEpochDay()

    @TypeConverter fun epochDayToLocalDate(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter fun yearMonthToString(value: YearMonth?): String? = value?.toString()

    @TypeConverter fun stringToYearMonth(value: String?): YearMonth? = value?.let(YearMonth::parse)
}
