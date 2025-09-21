package edu.example.mycontacts


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import edu.example.mycontacts.databinding.ContactListItemBinding
import edu.example.mycontacts.model.Contact
import edu.example.mycontacts.helper.ItemTouchHelpersContract
import java.util.Collections


class ContactsAdapter(var contacts: MutableList<Contact>, val mainActivity: MainActivity) :
    Adapter<ContactsAdapter.ContactViewHolder>(), ItemTouchHelpersContract {

    public fun setContact(contacts: MutableList<Contact>) {
        this.contacts = contacts
    }


//    class ContactViewHolder : RecyclerView.ViewHolder {
//
//        val firstNameText: TextView
//        val lastNameText: TextView
//        val emailText: TextView
//        val numberPhoneText: TextView
//
//        constructor(itemView: View) : super(itemView) {
//            firstNameText = itemView.findViewById(R.id.firstNameTextView)
//            lastNameText = itemView.findViewById(R.id.lastNameTextView)
//            emailText = itemView.findViewById(R.id.emailTextView)
//            numberPhoneText = itemView.findViewById(R.id.numberPhoneTextView)
//        }
//    }

    class ContactViewHolder( var contactListItemBinding: ContactListItemBinding) :
        RecyclerView.ViewHolder(contactListItemBinding.root) {

//        private lateinit var contactListItemBinding: ContactListItemBinding


//        constructor(contactListItemBinding: ContactListItemBinding) {
//            this.contactListItemBinding = contactListItemBinding
//        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
//        val itemView =
//            LayoutInflater.from(parent.context).inflate(R.layout.contact_list_item, parent, false)
//        return ContactViewHolder(itemView)

        val contactListItemBinding = DataBindingUtil.inflate<ContactListItemBinding>(
            LayoutInflater.from(parent.context), R.layout.contact_list_item, parent,false
        )
        return ContactViewHolder(contactListItemBinding)
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {

        val contact = contacts[position]

//        holder.firstNameText.text = contact.firstName
//        holder.lastNameText.text = contact.lastName
//        holder.emailText.text = contact.email
//        holder.numberPhoneText.text = contact.phoneNumber

        holder.contactListItemBinding.contact = contact

        holder.itemView.setOnClickListener {
            mainActivity.addAndEditContact(true, contact, position)
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(contacts, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(contacts, i, i - 1)
            }
        }

        notifyItemMoved(fromPosition, toPosition)


    }

    override fun onItemDismiss(viewHolder: ViewHolder, directory: Int) {
        val position = viewHolder.absoluteAdapterPosition
        when (directory) {
            ItemTouchHelper.LEFT -> {
                Log.d("Delete", "Delete item ${contacts[position].firstName}")
                mainActivity.deleteContact(contacts[position], position)
                notifyItemRemoved(position)
            }

            ItemTouchHelper.RIGHT -> {
                Log.d("Update", "Update item ${contacts[position].firstName}")
                mainActivity.addAndEditContact(
                    true, contacts[position], position
                )
                notifyItemChanged(position)
            }
        }
    }

}