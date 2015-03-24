package com.github.tototoshi.play.social.twitter

import com.github.tototoshi.play.social.core.OAuth10aAuthenticator
import play.api.Logger
import play.api.Play.current
import play.api.libs.oauth.{ ConsumerKey, OAuthCalculator, RequestToken }
import play.api.libs.ws.{ WS, WSResponse }

import scala.concurrent.Future

case class TwitterUser(
  id: Long,
  screenName: String,
  name: String,
  description: String,
  profileImageUrl: String,
  accessToken: String,
  accessTokenSecret: String)

class TwitterOAuth10aAuthenticator extends OAuth10aAuthenticator {

  type AccessToken = TwitterOAuth10aAccessToken

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

}

