package com.github.tototoshi.play.social.facebook.oauth2

import com.github.tototoshi.play.social.core.OAuthProviderUserSupport
import play.api.Logger
import play.api.libs.ws.{ WS, WSResponse }
import play.api.Play.current
import scala.concurrent.{ ExecutionContext, Future }

trait FacebookProviderUserSupport extends OAuthProviderUserSupport {
  self: FacebookController =>

  type ProviderUser = FacebookUser

  private def readProviderUser(accessToken: String, response: WSResponse): ProviderUser = {
    val j = response.json
    FacebookUser(
      (j \ "id").as[String],
      (j \ "name").as[String],
      (j \ "picture" \ "data" \ "url").as[String],
      accessToken
    )
  }

  def retrieveProviderUser(accessToken: AccessToken)(implicit ctx: ExecutionContext): Future[ProviderUser] = {
    for {
      response <- WS.url("https://graph.facebook.com/me")
        .withQueryString("access_token" -> accessToken, "fields" -> "name,first_name,last_name,picture.type(large),email")
        .get()
    } yield {
      Logger(getClass).debug("Retrieving user info from provider API: " + response.body)
      readProviderUser(accessToken, response)
    }
  }

}
