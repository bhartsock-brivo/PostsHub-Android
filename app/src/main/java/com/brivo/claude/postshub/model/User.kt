package com.brivo.claude.postshub.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val phone: String,
    val image: String,
    val address: Address,
) {
    val name: String get() = "$firstName $lastName"
}

@Serializable
data class Address(
    val address: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val coordinates: Coordinates,
)

@Serializable
data class Coordinates(
    val lat: String,
    val lng: String,
)
