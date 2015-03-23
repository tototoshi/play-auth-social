package com.github.tototoshi.play.social.facebook

import com.github.tototoshi.play.social.core.OAuth2Controller
import jp.t2v.lab.play2.auth.{AuthConfig, Login, OptionalAuthElement}

trait FacebookOAuth2Controller extends OAuth2Controller
  with AuthConfig
  with OptionalAuthElement
  with Login