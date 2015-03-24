package com.github.tototoshi.play.social.core

class AccessTokenRetrievalFailedException(message: String, exception: Throwable)
    extends RuntimeException(message, exception) {

  def this(message: String) = this(message, null)

}
