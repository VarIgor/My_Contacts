package edu.example.mycontacts

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import edu.example.mycontacts.ui.viewmodel.ContactViewModel
import edu.example.mycontacts.ui.viewmodel.ContactViewModelFactory
import edu.example.mycontacts.utils.Util
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnContactClickListener, OnOrderChangedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var viewModel: ContactViewModel
    private lateinit var recyclerView: RecyclerView

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
            viewModel.snackbarMessage.collect { message ->
                message?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = contactsAdapter
    }

    fun showAddEditDialog(isUpdate: Boolean, contact: Contact?) {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.layout_add_contact, null)

        val dialogTitle = view.findViewById<TextView>(R.id.addContactTitle)
        val firstNameEdit = view.findViewById<EditText>(R.id.firstNameEditText)
        val lastNameEdit = view.findViewById<EditText>(R.id.lastNameEditText)
        val emailEdit = view.findViewById<EditText>(R.id.emailEditText)
        val phoneNumberEdit = view.findViewById<EditText>(R.id.phoneNumberEditText)

        dialogTitle.text = if (!isUpdate) "Add contact" else "Edit contact"

        if (isUpdate && contact != null) {
            firstNameEdit.setText(contact.firstName)
            lastNameEdit.setText(contact.lastName)
            emailEdit.setText(contact.email)
            phoneNumberEdit.setText(contact.phoneNumber)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .setPositiveButton(if (isUpdate) "Update" else "Save", null)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val firstName = firstNameEdit.text.toString()
            val lastName = lastNameEdit.text.toString()
            val email = emailEdit.text.toString()
            val phoneNumber = phoneNumberEdit.text.toString()

            when {
                TextUtils.isEmpty(firstName) -> toast("Enter first name")
                TextUtils.isEmpty(lastName) -> toast("Enter last name")
                TextUtils.isEmpty(email) -> toast("Enter email")
                TextUtils.isEmpty(phoneNumber) -> toast("Enter phone number")
                else -> {
                    dialog.dismiss()
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
                }
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onContactClick(contact: Contact) {
        showAddEditDialog(true, contact)
    }

    override fun onContactDelete(contact: Contact) {
        viewModel.deleteContact(contact)
    }

    override fun onContactEdit(contact: Contact) {
        showAddEditDialog(true, contact)
    }

    override fun onOrderChanged(contacts: List<Contact>) {
        viewModel.updateContactsOrder(contacts)
    }

    inner class MainActivityButtonHandler() {
        fun onButtonClick(view: View) {
            showAddEditDialog(false, null)
        }
    }
}