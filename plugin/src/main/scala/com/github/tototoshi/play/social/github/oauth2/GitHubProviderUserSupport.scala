package com.github.tototoshi.play.social.github.oauth2

import com.github.tototoshi.play.social.core.OAuthProviderUserSupport
import play.api.Play.current
import play.api.libs.ws.{ WS, WSResponse }

import scala.concurrent.{ ExecutionContext, Future }

trait GitHubProviderUserSupport extends OAuthProviderUserSupport {
  self: GitHubController =>

  type ProviderUser = GitHubUser

  private def readProviderUser(accessToken: String, response: WSResponse): ProviderUser = {
    val j = response.json
    GitHubUser(
      (j \ "id").as[Long],
      (j \ "login").as[String],
      (j \ "avatar_url").as[String],
      accessToken
    )
  }

  def retrieveProviderUser(accessToken: String)(implicit ctx: ExecutionContext): Future[ProviderUser] = {
    for {
      response <- WS.url("https://api.github.com/user").withHeaders("Authorization" -> s"token ${accessToken}").get()
    } yield {
      readProviderUser(accessToken, response)
    }
  }

}
