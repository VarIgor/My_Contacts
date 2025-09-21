package edu.example.mycontacts.helper

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import edu.example.mycontacts.ContactsAdapter


interface ItemTouchHelpersContract {
    fun onRowMoved(fromPosition: Int, toPosition: Int)
    fun onItemDismiss(viewHolder: ViewHolder, directory: Int)
}