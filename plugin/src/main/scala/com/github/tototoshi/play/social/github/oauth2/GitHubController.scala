package com.github.tototoshi.play.social.github.oauth2

import com.github.tototoshi.play.social.core.OAuth2Controller
import jp.t2v.lab.play2.auth.{ AuthConfig, Login, OptionalAuthElement }

trait GitHubController extends OAuth2Controller
    with AuthConfig
    with OptionalAuthElement
    with Login {

  val authenticator = new GitHubAuthenticator

}