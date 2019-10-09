package com.bootstrap.graphql.exceptions

import com.bootstrap.exceptions.CommonException

case class NotAuthorizedException(
    private val message: String = "Not Authorized"
) extends CommonException(key = "not_authorized", message, None.orNull)