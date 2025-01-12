package com.techbros.myproject.model

data class Test(
    var name: String? = "",
    var age: String? = "",
    var gender: String? = "",
    var remarks: String? = "",
    var shade: String = ""
) {
    // No-argument constructor
    constructor() : this("", "", "", "", "")
}
