package edu.example.mycontacts.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import edu.example.mycontacts.model.Contact

@Database(entities = [Contact::class], version = 2)
abstract class ContactsAppDatabase: RoomDatabase() {

    abstract fun getContactDao():ContactDao

    companion object{
        val MIGRATION_1_2 = object : Migration(1,2){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE contacts ADD COLUMN display_order INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }
}