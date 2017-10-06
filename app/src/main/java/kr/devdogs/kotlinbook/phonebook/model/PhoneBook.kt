package kr.devdogs.kotlinbook.phonebook.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class PhoneBook(@PrimaryKey var id: Int = 0,
                     var photoSrc: String? = null,
                     var name: String? = null,
                     var phone: String? = null,
                     var email: String? = null) : RealmObject()
