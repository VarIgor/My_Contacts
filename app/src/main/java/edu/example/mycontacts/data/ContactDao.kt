package edu.example.mycontacts.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import edu.example.mycontacts.model.Contact

@Dao
interface ContactDao {

    @Insert
     fun addContact(contact: Contact): Long

    @Update
     fun updateContact(contact: Contact)

    @Delete
     fun deleteContact(contact: Contact)

    @Query("select * from contacts")
     fun getAllContacts(): MutableList<Contact>

    @Query("select * from contacts where contact_id ==:contactId")
     fun getContact(contactId: Long): Contact

}