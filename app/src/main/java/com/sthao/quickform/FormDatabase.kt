package com.sthao.quickform

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sthao.quickform.util.Constants.DATABASE_NAME

@Database(entities = [FormEntry::class, FormImage::class], version = 8, exportSchema = false)
abstract class FormDatabase : RoomDatabase() {

    abstract fun formDao(): FormDao

    companion object {
        @Volatile
        private var INSTANCE: FormDatabase? = null

        /**
         * Migration to add all new signature columns to the form_entries table.
         * This ensures the database schema matches the latest FormEntry data class.
         */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE form_entries ADD COLUMN pickupPrintSignatureOne TEXT")
                db.execSQL("ALTER TABLE form_entries ADD COLUMN pickupSignatureOne BLOB")
                db.execSQL("ALTER TABLE form_entries ADD COLUMN pickupPrintSignatureTwo TEXT")
                db.execSQL("ALTER TABLE form_entries ADD COLUMN pickupSignatureTwo BLOB")
                db.execSQL("ALTER TABLE form_entries ADD COLUMN dropoffPrintSignatureOne TEXT")
                db.execSQL("ALTER TABLE form_entries ADD COLUMN dropoffSignatureOne BLOB")
                db.execSQL("ALTER TABLE form_entries ADD COLUMN dropoffPrintSignatureTwo TEXT")
                db.execSQL("ALTER TABLE form_entries ADD COLUMN dropoffSignatureTwo BLOB")
            }
        }

        /**
         * Migration to add indexes for better query performance.
         */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create indexes for frequently queried columns
                db.execSQL("CREATE INDEX IF NOT EXISTS index_form_entries_entryTitle ON form_entries(entryTitle)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_form_entries_pickupDate ON form_entries(pickupDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_form_entries_dropoffDate ON form_entries(dropoffDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_form_entries_pickupFacilityName ON form_entries(pickupFacilityName)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_form_entries_dropoffFacilityName ON form_entries(dropoffFacilityName)")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE form_entries ADD COLUMN pickupAdditionalNotes TEXT")
                db.execSQL("ALTER TABLE form_entries ADD COLUMN dropoffAdditionalNotes TEXT")
            }
        }

        fun getDatabase(context: Context): FormDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FormDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}