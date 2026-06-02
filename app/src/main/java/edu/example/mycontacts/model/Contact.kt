package edu.example.mycontacts.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
class Contact{

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "contact_id")
    var id:Long

    @ColumnInfo(name = "first_name")
    var firstName:String

    @ColumnInfo(name = "last_name")
    var lastName:String

    @ColumnInfo(name = "email")
    var email:String

    @ColumnInfo(name = "phone_number")
    var phoneNumber:String

    @ColumnInfo(name = "display_order", defaultValue = "0")
    var displayOrder: Int = 0

    constructor(id:Long, firstName: String, lastName: String, email: String, phoneNumber: String){
        this.id = id
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
        this.phoneNumber = phoneNumber
    }


}