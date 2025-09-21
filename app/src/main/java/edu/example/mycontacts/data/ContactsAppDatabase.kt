package edu.example.mycontacts.data

import androidx.room.Database
import androidx.room.RoomDatabase
import edu.example.mycontacts.model.Contact

@Database(entities = [Contact::class], version = 1)
abstract class ContactsAppDatabase: RoomDatabase() {

    abstract fun getContactDao():ContactDao
}