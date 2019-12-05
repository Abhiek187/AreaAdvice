package com.example.areaadvice.utils

fun miToM(miles: Float): Float {
    return miles * 1609.344f
}

fun kmToMi(kilos: Float): Float {
    return kilos / 1.609f
}

fun cToF(celsius: Float): Float {
    return celsius * 1.8f + 32
}

fun lxToFc(lux: Float): Float {
    return lux / 10.764f
}
