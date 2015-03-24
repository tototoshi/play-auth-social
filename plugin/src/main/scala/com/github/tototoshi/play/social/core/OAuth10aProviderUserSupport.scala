package com.github.tototoshi.play.social.core

import play.api.libs.oauth.{ ConsumerKey, RequestToken }

import scala.concurrent.Future

trait OAuth10aProviderUserSupport {
  self: OAuth10aController =>

  type ProviderUser

  def retrieveProviderUser(consumerKey: ConsumerKey, accessToken: AccessToken): Future[ProviderUser]

}
