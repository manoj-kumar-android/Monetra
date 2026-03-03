package com.monetra.data.local

import androidx.room.TypeConverter
import com.monetra.domain.model.TransactionType
import com.monetra.domain.model.GoalCategory
import com.monetra.domain.model.InvestmentType
import com.monetra.domain.model.ContributionFrequency
import com.monetra.domain.model.BillStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): String? {
        return date?.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @TypeConverter
    fun fromDateTimeTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun dateTimeToTimestamp(date: LocalDateTime?): String? {
        return date?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return enumValueOf<TransactionType>(value)
    }

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    @TypeConverter
    fun toGoalCategory(value: String): GoalCategory {
        return enumValueOf<GoalCategory>(value)
    }

    @TypeConverter
    fun fromGoalCategory(category: GoalCategory): String {
        return category.name
    }

    @TypeConverter
    fun toInvestmentType(value: String): InvestmentType {
        return enumValueOf<InvestmentType>(value)
    }

    @TypeConverter
    fun fromInvestmentType(type: InvestmentType): String {
        return type.name
    }
    @TypeConverter
    fun toContributionFrequency(value: String): com.monetra.domain.model.ContributionFrequency {
        return enumValueOf<com.monetra.domain.model.ContributionFrequency>(value)
    }

    @TypeConverter
    fun fromContributionFrequency(frequency: com.monetra.domain.model.ContributionFrequency): String {
        return frequency.name
    }
    @TypeConverter
    fun toBillStatus(value: String): com.monetra.domain.model.BillStatus {
        return enumValueOf<com.monetra.domain.model.BillStatus>(value)
    }

    @TypeConverter
    fun fromBillStatus(status: com.monetra.domain.model.BillStatus): String {
        return status.name
    }

    @TypeConverter
    fun fromYearMonth(value: java.time.YearMonth?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toYearMonth(value: String?): java.time.YearMonth? {
        return value?.let { java.time.YearMonth.parse(it) }
    }
}
