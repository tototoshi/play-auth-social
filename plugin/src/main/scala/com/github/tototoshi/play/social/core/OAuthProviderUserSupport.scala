package com.github.tototoshi.play.social.core

import scala.concurrent.{ ExecutionContext, Future }

trait OAuthProviderUserSupport {
  self: OAuthControllerBase =>

  type ProviderUser

  def retrieveProviderUser(accessToken: AccessToken)(implicit ctx: ExecutionContext): Future[ProviderUser]

}
