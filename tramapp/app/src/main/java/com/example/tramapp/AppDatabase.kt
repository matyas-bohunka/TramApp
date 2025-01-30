package com.example.tramapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TramType::class, Measurement::class, WheelMeasurement::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tramTypeDao(): TramTypeDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun wheelMeasurementDao(): WheelMeasurementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase { // Pass CoroutineScope to getDatabase
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tram_tire_database"
                )
                    .addCallback(TramTypeDatabaseCallback(scope)) // Add the callback
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class TramTypeDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.tramTypeDao())
                }
            }
        }

        suspend fun populateDatabase(tramTypeDao: TramTypeDao) {
            // Delete all content here.
            // tramTypeDao.deleteAll() // If you want to clear existing data on each install (optional)

            // Add sample tram types.
            var tramType = TramType(name = "CAF 3/5", numAxles = 6)
            tramTypeDao.insert(tramType)
            tramType = TramType(name = "CAF 3/9", numAxles = 10)
            tramTypeDao.insert(tramType)
            tramType = TramType(name = "Siemens Combino", numAxles = 12)
            tramTypeDao.insert(tramType)
            tramType = TramType(name = "T5C5/T5C5K", numAxles = 4)
            tramTypeDao.insert(tramType)
            tramType = TramType(name = "KCSV7", numAxles = 8)
            tramTypeDao.insert(tramType)
            tramType = TramType(name = "GANZ-CSUKLÓS", numAxles = 8)
            tramTypeDao.insert(tramType)
            tramType = TramType(name = "TW6000/TW6100", numAxles = 8)
            tramTypeDao.insert(tramType)
            // Add more tram types as needed
        }
    }
}