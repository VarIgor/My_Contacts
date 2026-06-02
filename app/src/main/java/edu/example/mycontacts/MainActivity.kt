package edu.example.mycontacts

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import edu.example.mycontacts.data.ContactsAppDatabase
import edu.example.mycontacts.databinding.ActivityMainBinding
import edu.example.mycontacts.model.Contact
import edu.example.mycontacts.helper.ItemMoveCallback
import edu.example.mycontacts.helper.OnContactClickListener
import edu.example.mycontacts.utils.Util
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), OnContactClickListener, OnOrderChangedListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var buttonHandler: MainActivityButtonHandler
    private lateinit var recyclerView: RecyclerView
    private var contactsList = mutableListOf<Contact>()
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var contactsAppDatabase: ContactsAppDatabase

    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        buttonHandler = MainActivityButtonHandler(this)
        binding.buttonHandler = this.buttonHandler

        contactsAppDatabase = Room.databaseBuilder(
            applicationContext,
            ContactsAppDatabase::class.java,
            Util.DATABASE_NAME
        )
            .addMigrations(ContactsAppDatabase.MIGRATION_1_2)
            .build()

        getAllContacts()

        contactsAdapter = ContactsAdapter(contactsList, this, this)

        setupRecyclerView()

        val callback = ItemMoveCallback(contactsAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)

    }

    private fun setupRecyclerView() {
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = contactsAdapter
    }

    fun addAndEditContact(isUpdate: Boolean, contact: Contact?, position: Int) {
        val layoutInflaterAndroid = LayoutInflater.from(applicationContext)
        val view = layoutInflaterAndroid.inflate(R.layout.layout_add_contact, null)

        val alertDialogBuilderUserInput = AlertDialog.Builder(this)
        alertDialogBuilderUserInput.setView(view)

        val newContact = view.findViewById<TextView>(R.id.addContactTitle)
        val firstNameEdit = view.findViewById<EditText>(R.id.firstNameEditText)
        val lastNameEdit = view.findViewById<EditText>(R.id.lastNameEditText)
        val emailEdit = view.findViewById<EditText>(R.id.emailEditText)
        val phoneNumberEdit = view.findViewById<EditText>(R.id.phoneNumberEditText)

        newContact.text = if (!isUpdate) "Add contact" else "Edit contact"

        if (isUpdate && contact != null) {
            firstNameEdit.setText(contact.firstName)
            lastNameEdit.setText(contact.lastName)
            emailEdit.setText(contact.email)
            phoneNumberEdit.setText(contact.phoneNumber)
        }

        alertDialogBuilderUserInput.setCancelable(false)
            .setPositiveButton(
                if (isUpdate) "Update" else "Save",
                DialogInterface.OnClickListener { dialog, which -> })
            .setNegativeButton(
                "Cancel",
                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        val alertDialog = alertDialogBuilderUserInput.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (TextUtils.isEmpty(firstNameEdit.text.toString())) {
                Toast.makeText(this, "Enter first name please.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else if (TextUtils.isEmpty(lastNameEdit.text.toString())) {
                Toast.makeText(this, "Enter last name please.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else if (TextUtils.isEmpty(emailEdit.text.toString())) {
                Toast.makeText(this, "Enter email please.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else if (TextUtils.isEmpty(phoneNumberEdit.text.toString())) {
                Toast.makeText(this, "Enter phone number please.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else {
                alertDialog.dismiss()
            }

            if (isUpdate && contact != null) {
                updateContact(
                    Contact(
                        0,
                        firstNameEdit.text.toString(),
                        lastNameEdit.text.toString(),
                        emailEdit.text.toString(),
                        phoneNumberEdit.text.toString()
                    ), position
                )
            } else {
                createContact(
                    Contact(
                        0,
                        firstNameEdit.text.toString(),
                        lastNameEdit.text.toString(),
                        emailEdit.text.toString(),
                        phoneNumberEdit.text.toString()
                    )
                )
            }
        }

    }

    fun createContact(contact: Contact) {
        contact.displayOrder = contactsList.size
        executor.execute {
            val id = contactsAppDatabase.getContactDao().addContact(contact)
            val newContact = contactsAppDatabase.getContactDao().getContact(id)
            handler.post {
                if (newContact != null) {
                    contactsList.add(newContact)
                    contactsAdapter.notifyItemInserted(contactsList.size - 1)
                }
            }
        }
    }

    fun getAllContacts() {
        executor.execute {
            contactsList = contactsAppDatabase.getContactDao().getAllContacts()
            handler.post {
                contactsAdapter.setContact(contactsList)
                contactsAdapter.notifyDataSetChanged()
            }
        }

    }

    open fun updateContact(contact: Contact, position: Int) {

        val updateContact = contactsList[position]
        updateContact.firstName = contact.firstName
        updateContact.lastName = contact.lastName
        updateContact.email = contact.email
        updateContact.phoneNumber = contact.phoneNumber

        executor.execute {
            contactsAppDatabase.getContactDao().updateContact(updateContact)
            handler.post {
                contactsAdapter.notifyItemChanged(position)
            }
        }
    }

    open fun deleteContact(contact: Contact?, position: Int) {
        val contactToDelete = contact ?: return

        executor.execute {
            contactsAppDatabase.getContactDao().deleteContact(contactToDelete)
            handler.post {
                if (position < contactsList.size && contactsList[position].id == contactToDelete.id) {
                    contactsList.removeAt(position)
                    contactsAdapter.notifyItemRemoved(position)
                    updateDisplayOrdersAfterDelete(position)
                }
            }
        }
    }

    private fun updateDisplayOrdersAfterDelete(deletePosition: Int) {
        for (i in deletePosition until contactsList.size) {
            contactsList[i].displayOrder = i
            val finalI = i
            executor.execute {
                contactsAppDatabase.getContactDao().updateContact(contactsList[finalI])
            }
        }
    }

    inner class MainActivityButtonHandler(context: Context) {
        fun onButtonClick(view: View) {
             addAndEditContact(false, null, 0)
        }
    }

    override fun onContactClick(
        contact: Contact,
        position: Int
    ) {
        addAndEditContact(true, contact, position)
    }

    override fun onContactDelete(
        contact: Contact,
        position: Int
    ) {
        deleteContact(contact, position)
    }

    override fun onContactEdit(
        contact: Contact,
        position: Int
    ) {
        addAndEditContact(true, contact, position)
    }

    override fun onOrderChanged(contacts: List<Contact>) {
        executor.execute {
            contacts.forEach { contact ->
                contactsAppDatabase.getContactDao().updateContact(contact)
            }
            handler.post {
                Snackbar.make(
                    binding.root,
                    "Order saved",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}