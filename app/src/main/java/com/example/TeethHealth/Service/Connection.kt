package com.example.TeethHealth.Service

data class Connection (var isLogIn: Boolean,
                       var userName: String? = null,
                       var serviceAddress: String? = null,
                       var idDevice: String? = null)
    : java.io.Serializable

