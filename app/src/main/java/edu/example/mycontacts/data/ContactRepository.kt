package edu.example.mycontacts.data

import edu.example.mycontacts.model.Contact
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {
    fun getAllContacts(): Flow<List<Contact>> = contactDao.getAllContacts()
    suspend fun addContact(contact: Contact): Long = contactDao.addContact(contact)
    suspend fun updateContact(contact: Contact) = contactDao.updateContact(contact)
    suspend fun updateContacts(contacts: List<Contact>) = contactDao.updateContacts(contacts)
    suspend fun deleteContact(contact: Contact) = contactDao.deleteContact(contact)
}