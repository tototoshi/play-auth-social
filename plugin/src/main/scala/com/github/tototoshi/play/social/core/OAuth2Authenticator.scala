package com.github.tototoshi.play.social.core

import play.api.libs.ws.WSResponse

import scala.concurrent.{ ExecutionContext, Future }

trait OAuth2Authenticator extends OAuthAuthenticatorBase {

  val providerName: String

  val callbackUrl: String

  val accessTokenUrl: String

  val authorizationUrl: String

  val clientId: String

  val clientSecret: String

  def retrieveAccessToken(code: String)(implicit ctx: ExecutionContext): Future[AccessToken]

  def getAuthorizationUrl(scope: String, state: String): String

  def parseAccessTokenResponse(response: WSResponse): String

}
