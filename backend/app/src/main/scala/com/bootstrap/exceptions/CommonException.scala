package com.bootstrap.exceptions

class CommonException(
    private val key: String = "common_exception",
    private val message: String = "",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause) {
    def getKey = key
}