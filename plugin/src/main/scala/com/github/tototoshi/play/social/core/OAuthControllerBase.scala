package com.github.tototoshi.play.social.core

import jp.t2v.lab.play2.auth.{ AuthConfig, OptionalAuthElement }
import play.api.mvc.{ Result, RequestHeader }

import scala.concurrent.{ ExecutionContext, Future }

trait OAuthControllerBase { self: OptionalAuthElement with AuthConfig =>

  val authenticator: OAuthAuthenticatorBase

  type AccessToken = authenticator.AccessToken

  def gotoOAuthLoginSucceeded(token: AccessToken)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result]

  def gotoOAuthLinkSucceeded(token: AccessToken, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result]

  protected lazy val OAuthExecutionContext: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext

}
