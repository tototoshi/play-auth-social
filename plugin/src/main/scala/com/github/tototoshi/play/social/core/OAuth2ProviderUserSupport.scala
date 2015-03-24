package com.github.tototoshi.play.social.core

import scala.concurrent.Future

trait OAuth2ProviderUserSupport {
  self: OAuth2Controller =>

  type ProviderUser

  def retrieveProviderUser(accessToken: AccessToken): Future[ProviderUser]

}
