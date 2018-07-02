package com.lohjason.genericqr.util

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import com.google.android.gms.vision.barcode.Barcode


/**
 * IntentUtils
 * Created by jason on 26/6/18.
 */
class IntentUtils {

    companion object {

        private const val GOOGLE_SEARCH_URL_PREFIX = "https://www.google.com/search?q="

        fun getSaveContactIntent(contact: Barcode.ContactInfo): Intent {
            val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CONTACTS)
//            intent.type = ContactsContract.Contacts.CONTENT_TYPE

            contact.name?.let {
                intent.putExtra(ContactsContract.Intents.Insert.NAME, it.formattedName)
            }
            contact.organization?.let {
                intent.putExtra(ContactsContract.Intents.Insert.COMPANY, it)
            }
            contact.title?.let {
                intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, it)
            }
            contact.phones?.let {
                if (it.size > 2) {
                    intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, it[2].number)
                    intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE, it[2].type)
                }
                if (it.size > 1) {
                    intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, it[1].number)
                    intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE, it[1].type)
                }
                if (it.isNotEmpty()) {
                    intent.putExtra(ContactsContract.Intents.Insert.PHONE, it[0].number)
                    intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, it[0].type)
                }
            }
            contact.emails?.let {
                if (it.size > 2) {
                    intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL, it[2].address)
                    intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL_TYPE, it[2].type)
                }
                if (it.size > 1) {
                    intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, it[1].address)
                    intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE, it[1].type)
                }
                if (it.isNotEmpty()) {
                    intent.putExtra(ContactsContract.Intents.Insert.EMAIL, it[0].address)
                    intent.putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, it[0].type)
                }
            }
            contact.addresses?.let {
                if (it.isNotEmpty()) {
                    var fullAddress = ""
                    for (address in it[0].addressLines) {
                        fullAddress += address
                    }
                    intent.putExtra(ContactsContract.Intents.Insert.POSTAL, fullAddress)
                    intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, it[0].type)
                }
            }

            return intent
        }

        fun getSavePhoneNumberContactIntent(phoneNumber: String): Intent {
            val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CONTACTS)
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber)
            return intent
        }

        fun getWebSearchIntent(context: Context, data: String): Intent {
            val intent = Intent(Intent.ACTION_WEB_SEARCH)
            intent.putExtra(SearchManager.QUERY, data)
            return if (canIntentBeHandled(context, intent)) {
                intent
            } else {
                val url = GOOGLE_SEARCH_URL_PREFIX + data
                val uri = Uri.parse(url)
                Intent(Intent.ACTION_VIEW, uri)
            }
        }

        fun canIntentBeHandled(context: Context, intent: Intent): Boolean {
            val packageManager = context.packageManager
            intent.resolveActivity(packageManager)?.let {
                return true
            }
            return false
        }
    }
}