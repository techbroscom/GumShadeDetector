package com.techbros.myproject

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

enum class GumShade(val hexCode: String) {
    ZL_G("#D0887D"),
    G1("#D0887D"),
    G2("#BF735C"),
    G3("#BD8A8A"),
    G4("#BE7065"),
    G5("#92676F"),
    IG1("#C16045"),
    IG2("#B45147"),
    IG3("#D6918C"),
    IG4("#7D545D"),
    E20("#9C4458"),
    E21("#792936"),
    E22("#662A38");

    fun getRGB(): Triple<Int, Int, Int> {
        val colorInt = hexCode.substring(1).toInt(16)
        val r = (colorInt shr 16) and 0xFF
        val g = (colorInt shr 8) and 0xFF
        val b = colorInt and 0xFF
        return Triple(r, g, b)
    }
}