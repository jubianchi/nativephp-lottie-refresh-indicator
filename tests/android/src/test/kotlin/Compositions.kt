package com.jubianchi.nativephp.lottierefreshindicator

import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory

/** A 120-frame, 60 fps composition, like the app's `refresh.lottie`, with the given markers. */
fun composition(vararg markers: Triple<String, Int, Int>): LottieComposition {
    val json = markers.joinToString(",") { (name, frame, duration) -> """{"cm":"$name","tm":$frame,"dr":$duration}""" }
    return LottieCompositionFactory
        .fromJsonStringSync("""{"v":"5.7.4","fr":60,"ip":0,"op":120,"w":10,"h":10,"layers":[],"markers":[$json]}""", null)
        .value!!
}

val PULL = Triple("pull", 0, 60)

val REFRESH = Triple("refresh", 60, 60)
