package com.sthao.quickform

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sthao.quickform.util.Constants.DATABASE_NAME

@Database(entities = [FormEntry::class, FormImage::class, StationsItemSectionEntity::class], version = 12, exportSchema = false)
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

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add Stations columns
                db.execSQL("ALTER TABLE form_entries ADD COLUMN stationsRun TEXT")
                db.execSQL("ALTER TABLE form_entries ADD COLUMN stationsDate TEXT")
                db.execSQL("ALTER TABLE form_entries ADD COLUMN stationsDriverName TEXT")
                db.execSQL("ALTER TABLE form_entries ADD COLUMN stationsDriverNumber TEXT")
                db.execSQL("ALTER TABLE form_entries ADD COLUMN stationsFacilityName TEXT")
                db.execSQL("ALTER TABLE form_entries ADD COLUMN stationsTotes TEXT")
                db.execSQL("ALTER TABLE form_entries ADD COLUMN stationsAddOns TEXT")
                db.execSQL("ALTER TABLE form_entries ADD COLUMN stationsExtra TEXT") // Renamed from stationsQty
                db.execSQL("ALTER TABLE form_entries ADD COLUMN stationsPrintSignatureOne TEXT")
                db.execSQL("ALTER TABLE form_entries ADD COLUMN stationsSignatureOne BLOB")
                
                // Add indexes for Stations
                db.execSQL("CREATE INDEX IF NOT EXISTS index_form_entries_stationsDate ON form_entries(stationsDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_form_entries_stationsFacilityName ON form_entries(stationsFacilityName)")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Rename stationsQty column to stationsExtra
                db.execSQL("ALTER TABLE form_entries ADD COLUMN stationsExtra TEXT")
                db.execSQL("UPDATE form_entries SET stationsExtra = stationsQty WHERE stationsQty IS NOT NULL")
                // Note: We can't easily drop the old column in SQLite, so we'll just leave it
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create the stations_item_sections table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `stations_item_sections` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `formEntryId` INTEGER NOT NULL,
                        `sectionIndex` INTEGER NOT NULL,
                        `totes` TEXT NOT NULL,
                        `addOns` TEXT NOT NULL,
                        `extra` TEXT NOT NULL,
                        `printName` TEXT NOT NULL,
                        `signature` BLOB,
                        FOREIGN KEY(`formEntryId`) REFERENCES `form_entries`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                
                // Create index for formEntryId
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stations_item_sections_formEntryId` ON `stations_item_sections` (`formEntryId`)")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add sectionIndex column to form_images table
                db.execSQL("ALTER TABLE form_images ADD COLUMN sectionIndex INTEGER NOT NULL DEFAULT -1")
            }
        }

        fun getDatabase(context: Context): FormDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FormDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}