package edu.example.mycontacts.helper

import androidx.recyclerview.widget.RecyclerView.ViewHolder


interface ItemTouchHelpersContract {
    fun onRowMoved(fromPosition: Int, toPosition: Int)
    fun onItemDismiss(viewHolder: ViewHolder, directory: Int)
    fun onDragFinished()
}