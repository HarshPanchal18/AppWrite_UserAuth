package com.example.appwrite_userauth.configs

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Account

object AppwriteManager {
    lateinit var client: Client
    lateinit var account: Account

    fun initialize(context: Context) {
        client = Client(context)
            .setEndpoint("https://cloud.appwrite.io/v1")
            .setProject("648b140a11900d9078cd")
            .setSelfSigned(status = true)

        account = Account(client)
    }
}
