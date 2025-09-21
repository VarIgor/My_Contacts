package edu.example.mycontacts

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.SparseArray
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
import edu.example.mycontacts.data.ContactsAppDatabase
import edu.example.mycontacts.databinding.ActivityMainBinding
import edu.example.mycontacts.model.Contact
import edu.example.mycontacts.helper.ItemMoveCallback
import edu.example.mycontacts.utils.Util
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

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

//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root) // view binding


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        buttonHandler = MainActivityButtonHandler(this)
        binding.buttonHandler = this.buttonHandler

        contactsAppDatabase = Room.databaseBuilder(
            applicationContext, ContactsAppDatabase::
            class.java, Util.DATABASE_NAME
        ).build()

        getAllContacts()

        contactsAdapter = ContactsAdapter(contactsList, this)

        setupRecyclerView()

        val callback = ItemMoveCallback(contactsAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)

//        binding.fab.setOnClickListener { addAndEditContact(false, null, 0) }
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
            .setPositiveButton(if (isUpdate) "Update" else "Save",
                DialogInterface.OnClickListener { dialog, which -> })
            .setNegativeButton(
                "Cancel",
                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        //           .setNegativeButton(if (isUpdate) "Delete" else "Cancel",
        //             DialogInterface.OnClickListener { dialog, which ->
//                    if (isUpdate) {
//                        deleteContact(contact, position)
//                    } else {
//                        dialog.cancel()
//                    }
//                })

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
        Thread(Runnable {
            executor.execute {
                val id = contactsAppDatabase.getContactDao().addContact(contact)
                val contact = contactsAppDatabase.getContactDao().getContact(id)
                if (contact != null) {
                    contactsList.add(0, contact)
                }
                handler.post {
                    getAllContacts()
                    contactsAdapter.notifyDataSetChanged()
                }
            }
        }).start()

    }

    fun getAllContacts() {
        Thread(Runnable {
            executor.execute {
                contactsList = contactsAppDatabase.getContactDao().getAllContacts()

                handler.post {
                    contactsAdapter.setContact(contactsList)
                    contactsAdapter.notifyDataSetChanged()
                }
            }
        }).start()

    }

    open fun updateContact(contact: Contact, position: Int) {

        val updateContact = contactsList.get(position)
        updateContact.firstName = contact.firstName
        updateContact.lastName = contact.lastName
        updateContact.email = contact.email
        updateContact.phoneNumber = contact.phoneNumber
        contactsList.set(position, updateContact)

        Thread(Runnable {
            executor.execute {
                contactsAppDatabase.getContactDao().updateContact(updateContact)
                handler.post {
                    getAllContacts()
                }
            }
        }).start()
    }


    open fun deleteContact(contact: Contact?, position: Int) {

        contactsList.removeAt(position)
        Thread(Runnable {
            executor.execute {
                contactsAppDatabase.getContactDao().deleteContact(contact as Contact)
                handler.post {
                    getAllContacts()
                }
            }
        }).start()
    }

    inner class MainActivityButtonHandler(context: Context) {
         fun onButtonClick(view: View) {
            binding.fab.setOnClickListener { addAndEditContact(false, null, 0) }
        }
    }

}