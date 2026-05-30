package edu.example.mycontacts.helper

import edu.example.mycontacts.model.Contact

interface OnContactClickListener {
    fun onContactClick(contact: Contact, position: Int)
    fun onContactDelete(contact: Contact, position: Int)
    fun onContactEdit(contact: Contact, position: Int)
}