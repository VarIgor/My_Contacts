package edu.example.mycontacts.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.example.mycontacts.data.ContactRepository
import edu.example.mycontacts.model.Contact
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private val _showUndoSnackbar = MutableStateFlow<Contact?>(null)
    val showUndoSnackbar: StateFlow<Contact?> = _showUndoSnackbar.asStateFlow()

    private val _simpleMessage = MutableStateFlow<String?>(null)
    val simpleMessage: StateFlow<String?> = _simpleMessage.asStateFlow()

    private var deleteJob: Job? = null
    private var pendingDeleteContact: Contact? = null
    private var isUndoActive = false

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            repository.getAllContacts().collectLatest { contactsList ->
                if (!isUndoActive) {
                    _contacts.value = contactsList
                }
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
            showSimpleMessage("Контакт добавлен")
        }
    }

    fun updateContact(contact: Contact) {
        viewModelScope.launch {
            repository.updateContact(contact)
            showSimpleMessage("Контакт обновлён")
        }
    }

    fun deleteContact(contact: Contact, showUndo: Boolean = true) {
        deleteJob?.cancel()

        pendingDeleteContact = contact
        isUndoActive = true
        _contacts.value = _contacts.value.filter { it.id != contact.id }
        _showUndoSnackbar.value = contact

        deleteJob = viewModelScope.launch {
            delay(3000)
            pendingDeleteContact?.let { contactToDelete ->
                repository.deleteContact(contactToDelete)
                showSimpleMessage("Контакт удалён")
                pendingDeleteContact = null
            }
            _showUndoSnackbar.value = null
            isUndoActive = false
        }
    }

    fun undoDelete(contact: Contact) {
        deleteJob?.cancel()
        deleteJob = null

        _showUndoSnackbar.value = null

        isUndoActive = false
        loadContacts()
        pendingDeleteContact = null
        showSimpleMessage("Удаление отменено")
    }

    fun updateContactsOrder(contacts: List<Contact>) {
        viewModelScope.launch {
            repository.updateContactsOrder(contacts)
            _contacts.value = contacts
        }
    }

    private fun showSimpleMessage(message: String) {
        _simpleMessage.value = message
        viewModelScope.launch {
            delay(2000)
            if (_simpleMessage.value == message) {
                _simpleMessage.value = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        deleteJob?.cancel()
    }
}