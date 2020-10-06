package com.bc.gordiansigner.helper.ext

fun CharSequence.replaceSpaces() = this.replace("\\s+".toRegex(), " ").trim()