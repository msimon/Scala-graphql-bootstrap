package com.bootstrap.graphql.exceptions

import com.bootstrap.exceptions.CommonException

case class DuplicateException(
    private val message: String = "",
    private val cause: Throwable = None.orNull
) extends CommonException(key = "duplicate_exception", message, cause)