package com.github.tototoshi.play.social.github

import com.github.tototoshi.play.social.core.OAuth2Controller
import jp.t2v.lab.play2.auth.{ AuthConfig, Login, OptionalAuthElement }

trait GitHubOAuth2Controller extends OAuth2Controller
  with AuthConfig
  with OptionalAuthElement
  with Login