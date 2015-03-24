package com.github.tototoshi.play.social.github

import java.net.URLEncoder

import com.github.tototoshi.play.social.core.OAuth2Authenticator
import play.api.Logger
import play.api.Play.current
import play.api.http.{ HeaderNames, MimeTypes }
import play.api.libs.ws.{ WS, WSResponse }
import play.api.mvc.Results

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class GitHubUser(
  id: Long,
  login: String,
  avatarUrl: String,
  accessToken: String)

class GitHubOAuth2Authenticator extends OAuth2Authenticator {

  type ProviderUser = GitHubUser

  val providerName: String = "github"

  val accessTokenUrl = "https://github.com/login/oauth/access_token"

  val authorizationUrl = "https://github.com/login/oauth/authorize"

  lazy val clientId = current.configuration.getString("github.clientId").getOrElse(sys.error("github.clientId is missing"))

  lazy val clientSecret = current.configuration.getString("github.clientSecret").getOrElse(sys.error("github.clientSecret is missing"))

  lazy val callbackUrl = current.configuration.getString("github.callbackURL").getOrElse(sys.error("github.callbackURL is missing"))

  def retrieveAccessToken(code: String): Future[String] = {
    WS.url(accessTokenUrl)
      .withQueryString(
        "client_id" -> clientId,
        "client_secret" -> clientSecret,
        "code" -> code)
      .withHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON)
      .post(Results.EmptyContent())
      .map { response =>
        Logger(getClass).debug("Retrieving access token from provider API: " + response.body)
        parseAccessTokenResponse(response)
      }
  }

  def getAuthorizationUrl(scope: String, state: String): String = {
    val encodedClientId = URLEncoder.encode(clientId, "utf-8")
    val encodedRedirectUri = URLEncoder.encode(callbackUrl, "utf-8")
    val encodedScope = URLEncoder.encode(scope, "utf-8")
    val encodedState = URLEncoder.encode(state, "utf-8")
    s"${authorizationUrl}?client_id=${encodedClientId}&redirect_uri=${encodedRedirectUri}&scope=${encodedScope}&state=${encodedState}"
  }

  def parseAccessTokenResponse(response: WSResponse): String = {
    Logger(getClass).debug("Parsing access token response: " + response.body)
    (response.json \ "access_token").as[String]
  }

  def readProviderUser(accessToken: String, response: WSResponse): ProviderUser = {
    val j = response.json
    GitHubUser(
      (j \ "id").as[Long],
      (j \ "login").as[String],
      (j \ "avatar_url").as[String],
      accessToken
    )
  }

  def retrieveProviderUser(accessToken: String): Future[ProviderUser] = {
    for {
      response <- WS.url("https://api.github.com/user").withHeaders("Authorization" -> s"token ${accessToken}").get()
    } yield {
      readProviderUser(accessToken, response)
    }
  }

}

