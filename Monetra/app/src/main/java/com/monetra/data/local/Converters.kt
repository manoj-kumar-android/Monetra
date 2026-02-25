package com.monetra.data.local

import androidx.room.TypeConverter
import com.monetra.domain.model.TransactionType
import com.monetra.domain.model.GoalCategory
import com.monetra.domain.model.InvestmentType
import java.time.LocalDate
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
}
