package com.github.tototoshi.play.social.core

import play.api.libs.ws.WSResponse

import scala.concurrent.Future

trait OAuth2Authenticator {

  type AccessToken

  val providerName: String

  val callbackUrl: String

  val accessTokenUrl: String

  val authorizationUrl: String

  val clientId: String

  val clientSecret: String

  def retrieveAccessToken(code: String): Future[AccessToken]

  def getAuthorizationUrl(scope: String, state: String): String

  def parseAccessTokenResponse(response: WSResponse): String

}
