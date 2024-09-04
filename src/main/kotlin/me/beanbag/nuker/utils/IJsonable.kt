package me.beanbag.nuker.utils

import com.google.gson.JsonElement

interface IJsonable {
    fun toJson(): JsonElement

    fun fromJson(json: JsonElement)
}