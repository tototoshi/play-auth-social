package com.github.tototoshi.play.social.core

import play.api.libs.oauth._
import play.api.libs.ws.WSResponse
import play.api.mvc._

import scala.concurrent.Future

trait OAuth10aAuthenticator {

  type AccessToken

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

