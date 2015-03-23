package com.github.tototoshi.play.social.twitter

import com.github.tototoshi.play.social.core.OAuth10aAuthenticator
import play.api.Logger
import play.api.Play.current
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.Future

case class TwitterUser(
  id: Long,
  screenName: String,
  name: String,
  description: String,
  profileImageUrl: String,
  accessToken: String,
  accessTokenSecret: String)

trait TwitterOAuth10aAuthenticator extends OAuth10aAuthenticator {

  type ProviderUser = TwitterUser

  val providerName: String = "twitter"

  val requestTokenURL = "https://api.twitter.com/oauth/request_token"

  val accessTokenURL = "https://api.twitter.com/oauth/access_token"

  val authorizationURL = "https://api.twitter.com/oauth/authorize"

  lazy val consumerKey = ConsumerKey(
    current.configuration.getString("twitter.consumerKey").getOrElse(sys.error("twitter.consumerKey is missing")),
    current.configuration.getString("twitter.consumerSecret").getOrElse(sys.error("twitter.consumerSecret is missing"))
  )

  lazy val callbackURL = current.configuration.getString("twitter.callbackURL").getOrElse(
    sys.error("twitter.callbackURL is missing")
  )

  def readProviderUser(accessToken: String, accessTokenSecret: String, response: WSResponse): ProviderUser = {
    val j = response.json
    TwitterUser(
      (j \ "id").as[Long],
      (j \ "screen_name").as[String],
      (j \ "name").as[String],
      (j \ "description").as[String],
      (j \ "profile_image_url").as[String],
      accessToken,
      accessTokenSecret
    )
  }

  def retrieveUser(accessToken: String, accessTokenSecret: String): Future[ProviderUser] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    for {
      response <- WS.url("https://api.twitter.com/1.1/account/verify_credentials.json")
        .sign(OAuthCalculator(consumerKey, RequestToken(accessToken, accessTokenSecret))).get()
    } yield {
      Logger.debug("Retrieving user info from Twitter API: " + response.body)
      readProviderUser(accessToken, accessTokenSecret, response)
    }
  }

}

