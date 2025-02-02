package mc.merge.util

import com.google.gson.JsonElement

interface IJsonable {
    fun toJson(): JsonElement

    fun fromJson(json: JsonElement)
}