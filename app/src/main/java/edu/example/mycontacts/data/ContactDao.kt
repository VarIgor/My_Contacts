package edu.example.mycontacts.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import edu.example.mycontacts.model.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Insert
    suspend fun addContact(contact: Contact): Long

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("SELECT * FROM contacts ORDER BY display_order ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("select * from contacts where contact_id ==:contactId")
    suspend fun getContact(contactId: Long): Contact

}