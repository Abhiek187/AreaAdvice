package com.example.areaadvice.utils

fun miToM(miles: Float): Float {
    return miles * 1609.344f
}

fun cToF(celsius: Float): Float {
    return celsius * 1.8f + 32
}

fun lxToFc(lux: Float): Float {
    return lux / 10.764f
}
