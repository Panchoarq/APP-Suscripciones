package com.pancho.suscripciones.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromCurrency(value: Currency): String = value.name

    @TypeConverter
    fun toCurrency(value: String): Currency = Currency.valueOf(value)

    @TypeConverter
    fun fromCycle(value: BillingCycle): String = value.name

    @TypeConverter
    fun toCycle(value: String): BillingCycle = BillingCycle.valueOf(value)
}

@Database(
    entities = [CategoryEntity::class, SubscriptionEntity::class, PaymentMethodEntity::class],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun paymentMethodDao(): PaymentMethodDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "suscripciones.db",
                )
                    // App en desarrollo, sin usuarios reales aún: migración destructiva simple.
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
