package edu.example.mycontacts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import edu.example.mycontacts.data.ContactRepository
import edu.example.mycontacts.data.ContactsAppDatabase
import edu.example.mycontacts.databinding.ActivityMainBinding
import edu.example.mycontacts.model.Contact
import edu.example.mycontacts.helper.ItemMoveCallback
import edu.example.mycontacts.helper.OnContactClickListener
import edu.example.mycontacts.ui.dialogs.AddEditContactDialog
import edu.example.mycontacts.ui.viewmodel.ContactViewModel
import edu.example.mycontacts.ui.viewmodel.ContactViewModelFactory
import edu.example.mycontacts.utils.Util
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnContactClickListener, OnOrderChangedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var viewModel: ContactViewModel
    private lateinit var recyclerView: RecyclerView
    private var undoSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.buttonHandler = MainActivityButtonHandler()

        val database = Room.databaseBuilder(
            applicationContext,
            ContactsAppDatabase::class.java,
            Util.DATABASE_NAME
        )
            .addMigrations(ContactsAppDatabase.MIGRATION_1_2)
            .build()

        val repository = ContactRepository(database.getContactDao())
        val factory = ContactViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ContactViewModel::class.java]

        contactsAdapter = ContactsAdapter(mutableListOf(), this, this)
        setupRecyclerView()

        val callback = ItemMoveCallback(contactsAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)

        lifecycleScope.launch {
            viewModel.contacts.collect { contacts ->
                contactsAdapter.setContact(contacts.toMutableList())
            }
        }

        lifecycleScope.launch {
            viewModel.showUndoSnackbar.collect { contact ->
                contact?.let { showUndoSnackbar(it) }
            }
        }

        lifecycleScope.launch {
            viewModel.simpleMessage.collect { message ->
                message?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        setupSearchView()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchContacts(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchContacts(newText ?: "")
                return true
            }
        })
        binding.searchView.setOnCloseListener {
            viewModel.searchContacts("")
            false
        }
    }

    private fun showUndoSnackbar(contact: Contact) {
        undoSnackbar?.dismiss()

        undoSnackbar = Snackbar.make(
            binding.root,
            "Контакт будет удалён",
            Snackbar.LENGTH_LONG
        ).setAction("Отменить") {
            viewModel.undoDelete(contact)
            undoSnackbar = null
        }

        undoSnackbar?.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                undoSnackbar = null
            }
        })
        undoSnackbar?.show()
    }

    private fun setupRecyclerView() {
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = contactsAdapter
    }

    fun showAddEditDialog(isUpdate: Boolean, contact: Contact?, position: Int = -1) {
        val dialog = AddEditContactDialog(this, isUpdate, contact)
        dialog.show(
            onSave = { firstName, lastName, email, phoneNumber ->
                if (isUpdate && contact != null) {
                    val updateContact = Contact(
                        id = contact.id,
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        phoneNumber = phoneNumber
                    )
                    updateContact.displayOrder = contact.displayOrder
                    viewModel.updateContact(updateContact)
                } else {
                    viewModel.createContact(firstName, lastName, email, phoneNumber)
                }
            },
            onCancel = {
                if (isUpdate && position != -1) {
                    contactsAdapter.notifyItemChanged(position)
                }
            }
        )
    }

    override fun onContactClick(contact: Contact) {
        showAddEditDialog(true, contact)
    }

    override fun onContactDelete(contact: Contact) {
        viewModel.deleteContact(contact)
    }

    override fun onContactEdit(contact: Contact, position: Int) {
        showAddEditDialog(true, contact, position)
    }

    override fun onOrderChanged(contacts: List<Contact>) {
        viewModel.updateContactsOrder(contacts)
    }

    inner class MainActivityButtonHandler() {
        fun onButtonClick() {
            showAddEditDialog(false, null)
        }
    }
}