package com.sthao.quickform

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FormEntry::class, FormImage::class], version = 5, exportSchema = false)
abstract class FormDatabase : RoomDatabase() {

    abstract fun formDao(): FormDao

    companion object {
        @Volatile
        private var INSTANCE: FormDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE form_entries ADD COLUMN entryTitle TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE form_entries_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        entryTitle TEXT NOT NULL DEFAULT '',
                        pickupRun TEXT NOT NULL, pickupDate TEXT NOT NULL, pickupDriverName TEXT NOT NULL, pickupDriverNumber TEXT NOT NULL,
                        pickupFacilityName TEXT NOT NULL, pickupFrozenBags TEXT NOT NULL, pickupFrozenQuantity TEXT NOT NULL,
                        pickupRefrigeratedBags TEXT NOT NULL, pickupRefrigeratedQuantity TEXT NOT NULL,
                        pickupRoomTempBags TEXT NOT NULL, pickupRoomTempQuantity TEXT NOT NULL,
                        pickupOthersBags TEXT NOT NULL, pickupOthersQuantity TEXT NOT NULL,
                        pickupBoxesQuantity TEXT NOT NULL, pickupColoredBagsQuantity TEXT NOT NULL, pickupMailsQuantity TEXT NOT NULL,
                        pickupMoneyBagsQuantity TEXT NOT NULL, pickupNotes TEXT NOT NULL, pickupPrintSignature TEXT NOT NULL,
                        pickupSignature BLOB, pickupImage BLOB,
                        dropoffRun TEXT NOT NULL, dropoffDate TEXT NOT NULL, dropoffDriverName TEXT NOT NULL, dropoffDriverNumber TEXT NOT NULL,
                        dropoffFacilityName TEXT NOT NULL, dropoffFrozenBags TEXT NOT NULL, dropoffFrozenQuantity TEXT NOT NULL,
                        dropoffRefrigeratedBags TEXT NOT NULL, dropoffRefrigeratedQuantity TEXT NOT NULL,
                        dropoffRoomTempBags TEXT NOT NULL, dropoffRoomTempQuantity TEXT NOT NULL,
                        dropoffOthersBags TEXT NOT NULL, dropoffOthersQuantity TEXT NOT NULL,
                        dropoffBoxesQuantity TEXT NOT NULL, dropoffColoredBagsQuantity TEXT NOT NULL, dropoffMailsQuantity TEXT NOT NULL,
                        dropoffMoneyBagsQuantity TEXT NOT NULL, dropoffNotes TEXT NOT NULL, dropoffPrintSignature TEXT NOT NULL,
                        dropoffSignature BLOB, dropoffImage BLOB
                    )
                """)
                db.execSQL("""
                    INSERT INTO form_entries_new (id, entryTitle, pickupRun, pickupDate, pickupDriverName, pickupDriverNumber, pickupFacilityName, pickupFrozenBags, pickupFrozenQuantity, pickupRefrigeratedBags, pickupRefrigeratedQuantity, pickupRoomTempBags, pickupRoomTempQuantity, pickupOthersBags, pickupOthersQuantity, pickupBoxesQuantity, pickupColoredBagsQuantity, pickupMailsQuantity, pickupMoneyBagsQuantity, pickupNotes, pickupPrintSignature, dropoffRun, dropoffDate, dropoffDriverName, dropoffDriverNumber, dropoffFacilityName, dropoffFrozenBags, dropoffFrozenQuantity, dropoffRefrigeratedBags, dropoffRefrigeratedQuantity, dropoffRoomTempBags, dropoffRoomTempQuantity, dropoffOthersBags, dropoffOthersQuantity, dropoffBoxesQuantity, dropoffColoredBagsQuantity, dropoffMailsQuantity, dropoffMoneyBagsQuantity, dropoffNotes, dropoffPrintSignature)
                    SELECT id, entryTitle, pickupRun, pickupDate, pickupDriverName, pickupDriverNumber, pickupFacilityName, pickupFrozenBags, pickupFrozenQuantity, pickupRefrigeratedBags, pickupRefrigeratedQuantity, pickupRoomTempBags, pickupRoomTempQuantity, pickupOthersBags, pickupOthersQuantity, pickupBoxesQuantity, pickupColoredBagsQuantity, pickupMailsQuantity, pickupMoneyBagsQuantity, pickupNotes, pickupPrintSignature, dropoffRun, dropoffDate, dropoffDriverName, dropoffDriverNumber, dropoffFacilityName, dropoffFrozenBags, dropoffFrozenQuantity, dropoffRefrigeratedBags, dropoffRefrigeratedQuantity, dropoffRoomTempBags, dropoffRoomTempQuantity, dropoffOthersBags, dropoffOthersQuantity, dropoffBoxesQuantity, dropoffColoredBagsQuantity, dropoffMailsQuantity, dropoffMoneyBagsQuantity, dropoffNotes, dropoffPrintSignature FROM form_entries
                """)
                db.execSQL("DROP TABLE form_entries")
                db.execSQL("ALTER TABLE form_entries_new RENAME TO form_entries")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE form_entries_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        entryTitle TEXT NOT NULL DEFAULT '',
                        pickupRun TEXT, pickupDate TEXT, pickupDriverName TEXT, pickupDriverNumber TEXT,
                        pickupFacilityName TEXT, pickupFrozenBags TEXT, pickupFrozenQuantity TEXT,
                        pickupRefrigeratedBags TEXT, pickupRefrigeratedQuantity TEXT,
                        pickupRoomTempBags TEXT, pickupRoomTempQuantity TEXT,
                        pickupOthersBags TEXT, pickupOthersQuantity TEXT,
                        pickupBoxesQuantity TEXT, pickupColoredBagsQuantity TEXT, pickupMailsQuantity TEXT,
                        pickupMoneyBagsQuantity TEXT, pickupNotes TEXT, pickupPrintSignature TEXT,
                        pickupSignature BLOB, pickupImage BLOB,
                        dropoffRun TEXT, dropoffDate TEXT, dropoffDriverName TEXT, dropoffDriverNumber TEXT,
                        dropoffFacilityName TEXT, dropoffFrozenBags TEXT, dropoffFrozenQuantity TEXT,
                        dropoffRefrigeratedBags TEXT, dropoffRefrigeratedQuantity TEXT,
                        dropoffRoomTempBags TEXT, dropoffRoomTempQuantity TEXT,
                        dropoffOthersBags TEXT, dropoffOthersQuantity TEXT,
                        dropoffBoxesQuantity TEXT, dropoffColoredBagsQuantity TEXT, dropoffMailsQuantity TEXT,
                        dropoffMoneyBagsQuantity TEXT, dropoffNotes TEXT, dropoffPrintSignature TEXT,
                        dropoffSignature BLOB, dropoffImage BLOB
                    )
                """)

                val columns = arrayOf("id", "entryTitle", "pickupRun", "pickupDate", "pickupDriverName", "pickupDriverNumber", "pickupFacilityName", "pickupFrozenBags", "pickupFrozenQuantity", "pickupRefrigeratedBags", "pickupRefrigeratedQuantity", "pickupRoomTempBags", "pickupRoomTempQuantity", "pickupOthersBags", "pickupOthersQuantity", "pickupBoxesQuantity", "pickupColoredBagsQuantity", "pickupMailsQuantity", "pickupMoneyBagsQuantity", "pickupNotes", "pickupPrintSignature", "pickupSignature", "pickupImage", "dropoffRun", "dropoffDate", "dropoffDriverName", "dropoffDriverNumber", "dropoffFacilityName", "dropoffFrozenBags", "dropoffFrozenQuantity", "dropoffRefrigeratedBags", "dropoffRefrigeratedQuantity", "dropoffRoomTempBags", "dropoffRoomTempQuantity", "dropoffOthersBags", "dropoffOthersQuantity", "dropoffBoxesQuantity", "dropoffColoredBagsQuantity", "dropoffMailsQuantity", "dropoffMoneyBagsQuantity", "dropoffNotes", "dropoffPrintSignature", "dropoffSignature", "dropoffImage")
                val columnsString = columns.joinToString(", ")
                db.execSQL("INSERT INTO form_entries_new ($columnsString) SELECT $columnsString FROM form_entries")

                db.execSQL("DROP TABLE form_entries")
                db.execSQL("ALTER TABLE form_entries_new RENAME TO form_entries")
            }
        }

        /**
         * Migration to support multiple images per form entry.
         * This migration introduces a new 'form_images' table and removes the
         * old image columns from the 'form_entries' table.
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.beginTransaction()
                try {
                    // 1. Create the new 'form_images' table to store multiple images per form.
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `form_images` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `formEntryId` INTEGER NOT NULL,
                            `imageType` TEXT NOT NULL,
                            `imageData` BLOB NOT NULL,
                            FOREIGN KEY(`formEntryId`) REFERENCES `form_entries`(`id`) ON DELETE CASCADE
                        )
                    """)
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_form_images_formEntryId` ON `form_images` (`formEntryId`)")

                    // 2. Query the old table to get the image data and IDs before we modify it.
                    val cursor = db.query("SELECT id, pickupImage, dropoffImage FROM form_entries")
                    while (cursor.moveToNext()) {
                        val idColumnIndex = cursor.getColumnIndex("id")
                        val pickupImageColumnIndex = cursor.getColumnIndex("pickupImage")
                        val dropoffImageColumnIndex = cursor.getColumnIndex("dropoffImage")

                        val formId = cursor.getLong(idColumnIndex)

                        // 3. Move the pickup image (if it exists) to the new table.
                        if (!cursor.isNull(pickupImageColumnIndex)) {
                            val pickupImageBytes = cursor.getBlob(pickupImageColumnIndex)
                            db.execSQL(
                                "INSERT INTO form_images (formEntryId, imageType, imageData) VALUES (?, ?, ?)",
                                arrayOf(formId, "PICKUP", pickupImageBytes)
                            )
                        }

                        // 4. Move the dropoff image (if it exists) to the new table.
                        if (!cursor.isNull(dropoffImageColumnIndex)) {
                            val dropoffImageBytes = cursor.getBlob(dropoffImageColumnIndex)
                            db.execSQL(
                                "INSERT INTO form_images (formEntryId, imageType, imageData) VALUES (?, ?, ?)",
                                arrayOf(formId, "DROPOFF", dropoffImageBytes)
                            )
                        }
                    }
                    cursor.close()

                    // 5. Create a new 'form_entries' table without the old image columns.
                    // This is a "table rebuild" migration pattern.
                    db.execSQL("""
                        CREATE TABLE `form_entries_new` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `entryTitle` TEXT NOT NULL,
                            `pickupRun` TEXT, `pickupDate` TEXT, `pickupDriverName` TEXT, `pickupDriverNumber` TEXT, `pickupFacilityName` TEXT,
                            `pickupFrozenBags` TEXT, `pickupFrozenQuantity` TEXT, `pickupRefrigeratedBags` TEXT, `pickupRefrigeratedQuantity` TEXT,
                            `pickupRoomTempBags` TEXT, `pickupRoomTempQuantity` TEXT, `pickupOthersBags` TEXT, `pickupOthersQuantity` TEXT,
                            `pickupBoxesQuantity` TEXT, `pickupColoredBagsQuantity` TEXT, `pickupMailsQuantity` TEXT, `pickupMoneyBagsQuantity` TEXT,
                            `pickupNotes` TEXT, `pickupPrintSignature` TEXT, `pickupSignature` BLOB,
                            `dropoffRun` TEXT, `dropoffDate` TEXT, `dropoffDriverName` TEXT, `dropoffDriverNumber` TEXT, `dropoffFacilityName` TEXT,
                            `dropoffFrozenBags` TEXT, `dropoffFrozenQuantity` TEXT, `dropoffRefrigeratedBags` TEXT, `dropoffRefrigeratedQuantity` TEXT,
                            `dropoffRoomTempBags` TEXT, `dropoffRoomTempQuantity` TEXT, `dropoffOthersBags` TEXT, `dropoffOthersQuantity` TEXT,
                            `dropoffBoxesQuantity` TEXT, `dropoffColoredBagsQuantity` TEXT, `dropoffMailsQuantity` TEXT, `dropoffMoneyBagsQuantity` TEXT,
                            `dropoffNotes` TEXT, `dropoffPrintSignature` TEXT, `dropoffSignature` BLOB
                        )
                    """)

                    // 6. Copy all data (except images) from the old table to the new one.
                    val columns = "id, entryTitle, pickupRun, pickupDate, pickupDriverName, pickupDriverNumber, pickupFacilityName, pickupFrozenBags, pickupFrozenQuantity, pickupRefrigeratedBags, pickupRefrigeratedQuantity, pickupRoomTempBags, pickupRoomTempQuantity, pickupOthersBags, pickupOthersQuantity, pickupBoxesQuantity, pickupColoredBagsQuantity, pickupMailsQuantity, pickupMoneyBagsQuantity, pickupNotes, pickupPrintSignature, pickupSignature, dropoffRun, dropoffDate, dropoffDriverName, dropoffDriverNumber, dropoffFacilityName, dropoffFrozenBags, dropoffFrozenQuantity, dropoffRefrigeratedBags, dropoffRefrigeratedQuantity, dropoffRoomTempBags, dropoffRoomTempQuantity, dropoffOthersBags, dropoffOthersQuantity, dropoffBoxesQuantity, dropoffColoredBagsQuantity, dropoffMailsQuantity, dropoffMoneyBagsQuantity, dropoffNotes, dropoffPrintSignature, dropoffSignature"
                    db.execSQL("INSERT INTO form_entries_new ($columns) SELECT $columns FROM form_entries")

                    // 7. Drop the original table.
                    db.execSQL("DROP TABLE form_entries")

                    // 8. Rename the new table to the original name.
                    db.execSQL("ALTER TABLE form_entries_new RENAME TO form_entries")

                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }


        fun getDatabase(context: Context): FormDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FormDatabase::class.java,
                    "form_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}