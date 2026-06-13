package edu.example.mycontacts

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import edu.example.mycontacts.databinding.ContactListItemBinding
import edu.example.mycontacts.model.Contact
import edu.example.mycontacts.helper.ItemTouchHelpersContract
import edu.example.mycontacts.helper.OnContactClickListener
import java.util.Collections

class ContactsAdapter(
    var contacts: MutableList<Contact>,
    private val clickListener: OnContactClickListener,
    private val orderListener: OnOrderChangedListener
) : Adapter<ContactsAdapter.ContactViewHolder>(), ItemTouchHelpersContract {

    private var isDragging = false

    fun setContact(contacts: MutableList<Contact>) {
        this.contacts = contacts
        notifyDataSetChanged()
    }

    class ContactViewHolder(var contactListItemBinding: ContactListItemBinding) :
        ViewHolder(contactListItemBinding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {

        val contactListItemBinding = DataBindingUtil.inflate<ContactListItemBinding>(
            LayoutInflater.from(parent.context), R.layout.contact_list_item, parent, false
        )
        return ContactViewHolder(contactListItemBinding)
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {

        val contact = contacts[position]

        holder.contactListItemBinding.contact = contact
        holder.contactListItemBinding.executePendingBindings()
        holder.itemView.setOnClickListener {
            if (!isDragging) {
                clickListener.onContactClick(contact)
            }
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(contacts, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(contacts, i, i - 1)
            }
        }

        contacts.forEachIndexed { index, contact ->
            contact.displayOrder = index
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onDragFinished() {
        isDragging = false
        orderListener.onOrderChanged(contacts)
    }

    override fun onItemDismiss(viewHolder: ViewHolder, directory: Int) {
        val position = viewHolder.absoluteAdapterPosition
        if (position == RecyclerView.NO_POSITION) return

        when (directory) {
            ItemTouchHelper.LEFT -> {
                Log.d("Delete", "Delete item ${contacts[position].firstName}")
                val contact = contacts[position]
                clickListener.onContactDelete(contact)
            }

            ItemTouchHelper.RIGHT -> {
                Log.d("Update", "Update item ${contacts[position].firstName}")
                val contact = contacts[position]
                clickListener.onContactEdit(contact, position)
            }
        }
    }
}

interface OnOrderChangedListener {
    fun onOrderChanged(contacts: List<Contact>)
}