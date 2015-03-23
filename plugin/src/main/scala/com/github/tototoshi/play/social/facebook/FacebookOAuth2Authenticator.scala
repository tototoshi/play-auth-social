package com.github.tototoshi.play.social.facebook

import java.net.URLEncoder

import com.github.tototoshi.play.social.core.OAuth2Authenticator
import play.api.Logger
import play.api.Play.current
import play.api.http.{ HeaderNames, MimeTypes }
import play.api.libs.ws.{ WS, WSResponse }
import play.api.mvc.Results

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class FacebookUser(
  id: String,
  name: String,
  coverUrl: String,
  accessToken: String)

trait FacebookOAuth2Authenticator extends OAuth2Authenticator {

  type ProviderUser = FacebookUser

  val providerName: String = "facebook"

  val accessTokenUrl = "https://graph.facebook.com/oauth/access_token"

  val authorizationUrl = "https://graph.facebook.com/oauth/authorize"

  lazy val clientId = current.configuration.getString("facebook.clientId").getOrElse(sys.error("facebook.clientId is missing"))

  lazy val clientSecret = current.configuration.getString("facebook.clientSecret").getOrElse(sys.error("facebook.clientSecret is missing"))

  lazy val callbackUrl = current.configuration.getString("facebook.callbackURL").getOrElse(sys.error("facebook.callbackURL is missing"))

  def retrieveAccessToken(code: String): Future[String] = {
    WS.url(accessTokenUrl)
      .withQueryString(
        "client_id" -> clientId,
        "client_secret" -> clientSecret,
        "redirect_uri" -> callbackUrl,
        "code" -> code)
      .withHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON)
      .post(Results.EmptyContent())
      .map { response =>
        Logger.debug("Retrieving access token from provider API: " + response.body)
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
    Logger.debug("Parsing access token response: " + response.body)
    (for {
      params <- response.body.split("&").toList
      key :: value :: Nil = params.split("=").toList
      if key == "access_token"
    } yield {
      value
    }).headOption.getOrElse("Error") // TODO
  }

  def readProviderUser(accessToken: String, response: WSResponse): ProviderUser = {
    val j = response.json
    FacebookUser(
      (j \ "id").as[String],
      (j \ "name").as[String],
      (j \ "picture" \ "data" \ "url").as[String],
      accessToken
    )
  }

  def retrieveProviderUser(accessToken: String): Future[ProviderUser] = {
    for {
      response <- WS.url("https://graph.facebook.com/me")
        .withQueryString("access_token" -> accessToken, "fields" -> "name,first_name,last_name,picture.type(large),email")
        .get()
    } yield {
      Logger.debug("Retrieving user info from provider API: " + response.body)
      readProviderUser(accessToken, response)
    }
  }

}
