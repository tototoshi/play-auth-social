package com.github.tototoshi.play.social.core

import play.api.libs.oauth._

trait OAuth10aAuthenticator extends OAuthAuthenticatorBase {

  val callbackURL: String

  val requestTokenURL: String

  val accessTokenURL: String

  val authorizationURL: String

  val consumerKey: ConsumerKey

  lazy val serviceInfo: ServiceInfo = ServiceInfo(
    requestTokenURL,
    accessTokenURL,
    authorizationURL,
    consumerKey
  )

  lazy val oauth = OAuth(serviceInfo, use10a = true)

}

