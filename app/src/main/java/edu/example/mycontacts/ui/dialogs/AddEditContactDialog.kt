package edu.example.mycontacts.ui.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import edu.example.mycontacts.R
import edu.example.mycontacts.model.Contact

class AddEditContactDialog(
    private val context: Context,
    private val isUpdate: Boolean,
    private val contact: Contact?
) {

    private lateinit var firstNameEdit: EditText
    private lateinit var lastNameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var phoneNumberEdit: EditText

    private val originalFirstName: String
    private val originalLastName: String
    private val originalEmail: String
    private val originalPhone: String

    init {
        originalFirstName = contact?.firstName ?: ""
        originalLastName = contact?.lastName ?: ""
        originalEmail = contact?.email ?: ""
        originalPhone = contact?.phoneNumber ?: ""
    }

    fun show(
        onSave: (firstName: String, lastName: String, email: String, phoneNumber: String) -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_add_contact, null)
        setupViews(view)
        prefillFieldsIfUpdate()

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            .setPositiveButton(if (isUpdate) "Update" else "Save", null)
            .setNegativeButton("Cancel") { _, _ -> handleCancel(onCancel) }
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val result = getInputValues()
            if (validateInput(result)) {
                dialog.dismiss()
                onSave(
                    result.firstName,
                    result.lastName,
                    result.email,
                    result.phoneNumber
                )
            }
        }
    }

    private fun setupViews(view: View) {
        val dialogTitle = view.findViewById<TextView>(R.id.addContactTitle)
        dialogTitle.text = if (!isUpdate) "Add contact" else "Edit contact"

        firstNameEdit = view.findViewById<EditText>(R.id.firstNameEditText)
        lastNameEdit = view.findViewById<EditText>(R.id.lastNameEditText)
        emailEdit = view.findViewById<EditText>(R.id.emailEditText)
        phoneNumberEdit = view.findViewById<EditText>(R.id.phoneNumberEditText)
    }

    private fun prefillFieldsIfUpdate() {
        if (isUpdate && contact != null) {
            firstNameEdit.setText(contact.firstName)
            lastNameEdit.setText(contact.lastName)
            emailEdit.setText(contact.email)
            phoneNumberEdit.setText(contact.phoneNumber)
        }
    }

    private fun getInputValues(): InputResult {
        return InputResult(
            firstName = firstNameEdit.text.toString(),
            lastName = lastNameEdit.text.toString(),
            email = emailEdit.text.toString(),
            phoneNumber = phoneNumberEdit.text.toString()
        )
    }

    private fun validateInput(input: InputResult): Boolean {
        return when {
            input.firstName.isBlank() -> showError("Введите имя")
            input.lastName.isBlank() -> showError("Введите фамилию")
            input.email.isBlank() -> showError("Введите email")
            input.phoneNumber.isBlank() -> showError("Введите номер телефона")
            else -> true
        }
    }

    private fun showError(message: String): Boolean {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        return false
    }

    private fun handleCancel(onCancel: (() -> Unit)?) {
        if (isUpdate && hasChanges()) {
            showConfirmationDialog(
                title = "Отменить изменение",
                message = "Вы уверены, что хотите отменить изменения? Несохранёние данных будут потеряны",
                onConfirm = { onCancel?.invoke() ?: Unit }
            )
        } else {
            onCancel?.invoke()
        }
    }

    private fun hasChanges(): Boolean {
        val current = getInputValues()
        return current.firstName != originalFirstName ||
                current.lastName != originalLastName ||
                current.email != originalEmail ||
                current.phoneNumber != originalPhone
    }

    private fun showConfirmationDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Да") { _, _ -> onConfirm() }
            .setNegativeButton("Нет", null)
            .show()
    }

    private data class InputResult(
        val firstName: String,
        val lastName: String,
        val email: String,
        val phoneNumber: String
    )
}