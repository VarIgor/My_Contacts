package edu.example.mycontacts.data

import edu.example.mycontacts.model.Contact
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {
    fun getAllContacts(): Flow<List<Contact>> = contactDao.getAllContacts()
    suspend fun addContact(contact: Contact): Long = contactDao.addContact(contact)
    suspend fun updateContact(contact: Contact) = contactDao.updateContact(contact)
    suspend fun deleteContact(contact: Contact) = contactDao.deleteContact(contact)
    suspend fun getContact(contactId: Long): Contact = contactDao.getContact(contactId)
    suspend fun updateContactsOrder(contacts:List<Contact>){
        contacts.forEach { contact ->
            contactDao.updateContact(contact)
        }
    }
}