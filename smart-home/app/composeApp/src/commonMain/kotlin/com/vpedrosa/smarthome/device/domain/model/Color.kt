package com.vpedrosa.smarthome.device.domain.model

data class Color(val red: Int, val green: Int, val blue: Int) {
    init {
        require(red in 0..255) { "Red must be in 0..255, was $red" }
        require(green in 0..255) { "Green must be in 0..255, was $green" }
        require(blue in 0..255) { "Blue must be in 0..255, was $blue" }
    }

    companion object {
        val WHITE = Color(255, 255, 255)
        val WARM_WHITE = Color(255, 244, 229)
        val RED = Color(255, 0, 0)
        val GREEN = Color(0, 255, 0)
        val BLUE = Color(0, 0, 255)
    }
}
