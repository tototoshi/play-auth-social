package com.github.tototoshi.play.social.twitter.oauth10a

import com.github.tototoshi.play.social.core.OAuth10aController
import jp.t2v.lab.play2.auth.{ AuthConfig, Login, OptionalAuthElement }
import play.api.libs.oauth.RequestToken

trait TwitterController extends OAuth10aController
    with AuthConfig
    with OptionalAuthElement
    with Login {

  val authenticator = new TwitterAuthenticator

  def requestTokenToAccessToken(requestToken: RequestToken): AccessToken = {
    TwitterOAuth10aAccessToken(
      requestToken.token,
      requestToken.secret
    )
  }

}
