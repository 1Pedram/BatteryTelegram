package com.example.batterytg

data class DeviceInfo(
    val key: String,
    val name: String,
    val interval: Int,
    val threshold: Int,
    val isThisDevice: Boolean
)

/** Minimal parser for this app's own flat /devices JSON shape — no external JSON library needed. */
object DeviceListParser {

    fun parseDevices(body: String): List<DeviceInfo> {
        val arrayStart = body.indexOf("\"devices\"")
        if (arrayStart == -1) return emptyList()
        val bracketStart = body.indexOf('[', arrayStart)
        if (bracketStart == -1) return emptyList()

        val objects = mutableListOf<String>()
        var depth = 0
        var objStart = -1
        var i = bracketStart
        while (i < body.length) {
            when (body[i]) {
                '{' -> { if (depth == 0) objStart = i; depth++ }
                '}' -> {
                    depth--
                    if (depth == 0 && objStart != -1) {
                        objects.add(body.substring(objStart, i + 1))
                        objStart = -1
                    }
                }
                ']' -> if (depth == 0) { i = body.length; continue }
            }
            i++
        }

        return objects.mapNotNull { obj ->
            val key = Api.extractString(obj, "key") ?: return@mapNotNull null
            val name = Api.extractString(obj, "name") ?: "Unnamed device"
            val interval = Api.extractInt(obj, "interval") ?: 180
            val threshold = Api.extractInt(obj, "threshold") ?: 20
            val isThisDevice = Api.extractBool(obj, "isThisDevice") ?: false
            DeviceInfo(key, name, interval, threshold, isThisDevice)
        }
    }
}
