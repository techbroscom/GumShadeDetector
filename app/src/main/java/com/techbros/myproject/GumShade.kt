package com.techbros.myproject

enum class GumShade(val hexCode: String) {
    G1("#D0877C"),       // Previously ZL_G & G1 merged to G1
    G2("#BF735C"),
    G3("#BD8A8A"),
    G4("#BE7065"),
    G5("#92676F"),
    DG1("#C16045"),      // IG1 → DG1
    DG2("#B45147"),      // IG2 → DG2
    DG3("#D6918C"),      // IG3 → DG3
    DG4("#7D545D"),      // IG4 → DG4
    P1("#9C4458"),       // E20 → P1
    P2("#792936"),       // E21 → P2
    P3("#662A38");       // E22 → P3

    fun getRGB(): Triple<Int, Int, Int> {
        val colorInt = hexCode.substring(1).toInt(16)
        val r = (colorInt shr 16) and 0xFF
        val g = (colorInt shr 8) and 0xFF
        val b = colorInt and 0xFF
        return Triple(r, g, b)
    }
}