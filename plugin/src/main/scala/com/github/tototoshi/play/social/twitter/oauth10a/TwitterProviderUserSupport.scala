package com.github.tototoshi.play.social.twitter.oauth10a

import com.github.tototoshi.play.social.core.OAuthProviderUserSupport
import play.api.Logger
import play.api.Play.current
import play.api.libs.oauth.{ ConsumerKey, OAuthCalculator, RequestToken }
import play.api.libs.ws.{ WS, WSResponse }

import scala.concurrent.{ ExecutionContext, Future }

trait TwitterProviderUserSupport extends OAuthProviderUserSupport {
  self: TwitterController =>

  type ProviderUser = TwitterUser

  private def readProviderUser(accessToken: AccessToken, response: WSResponse): ProviderUser = {
    val j = response.json
    TwitterUser(
      (j \ "id").as[Long],
      (j \ "screen_name").as[String],
      (j \ "name").as[String],
      (j \ "description").as[String],
      (j \ "profile_image_url").as[String],
      accessToken.token,
      accessToken.secret
    )
  }

  def retrieveProviderUser(accessToken: AccessToken)(implicit ctx: ExecutionContext): Future[ProviderUser] = {
    for {
      response <- WS.url("https://api.twitter.com/1.1/account/verify_credentials.json")
        .sign(OAuthCalculator(authenticator.consumerKey, RequestToken(accessToken.token, accessToken.secret))).get()
    } yield {
      Logger(getClass).debug("Retrieving user info from Twitter API: " + response.body)
      readProviderUser(accessToken, response)
    }
  }

}
