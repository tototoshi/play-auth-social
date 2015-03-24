package com.github.tototoshi.play.social.slack.oauth2

import com.github.tototoshi.play.social.core.OAuth2Controller
import jp.t2v.lab.play2.auth.{ AuthConfig, Login, OptionalAuthElement }

trait SlackController extends OAuth2Controller
    with AuthConfig
    with OptionalAuthElement
    with Login {

  val authenticator = new SlackAuthenticator

}