package edu.example.mycontacts.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.example.mycontacts.data.ContactRepository
import edu.example.mycontacts.model.Contact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactViewModel(
    private val repository: ContactRepository
) : ViewModel() {
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            repository.getAllContacts().collectLatest { contactsList ->
                _contacts.value = contactsList
            }
        }
    }

    fun createContact(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String,
    ) {
        viewModelScope.launch {
            val currentSize = _contacts.value.size
            val newContact = Contact(
                id = 0,
                firstName = firstName,
                lastName = lastName,
                email = email,
                phoneNumber = phoneNumber,
            )
            newContact.displayOrder = currentSize
            repository.addContact(newContact)
            _snackbarMessage.value = "Контакт добавлен"
            clearSnackbarMessage()
        }
    }

    fun updateContact(contact: Contact) {
        viewModelScope.launch {
            repository.updateContact(contact)
            _snackbarMessage.value = "Контакт обновлён"
            clearSnackbarMessage()
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
            _snackbarMessage.value = "Контакт удалён"
            clearSnackbarMessage()
        }
    }

    fun updateContactsOrder(contacts: List<Contact>){
        viewModelScope.launch {
            repository.updateContactsOrder(contacts)
            _snackbarMessage.value = "Порядок сохранён"
        }
    }

    private fun clearSnackbarMessage() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _snackbarMessage.value = null
        }
    }
}