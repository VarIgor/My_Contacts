package edu.example.mycontacts.helper

import edu.example.mycontacts.model.Contact

interface OnContactClickListener {
    fun onContactClick(contact: Contact)
    fun onContactDelete(contact: Contact)
    fun onContactEdit(contact: Contact)
}