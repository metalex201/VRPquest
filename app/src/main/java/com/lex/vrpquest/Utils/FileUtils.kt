package com.lex.vrpquest.Utils

import java.io.File

fun getDirFullSize(File: File): Long {
    var size = 0L
    if (File.isDirectory) {
        for (file in File.listFiles()) {
            if (file.isDirectory) {
                size += getDirFullSize(file)
            }
            size += getDirFullSize(file)
        }
    } else {
        size += File.length()
    }
    return size
}