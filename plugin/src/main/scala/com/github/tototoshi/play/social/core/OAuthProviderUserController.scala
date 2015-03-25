package com.github.tototoshi.play.social.core

import jp.t2v.lab.play2.auth.{ AuthConfig, OptionalAuthElement }
import play.api.mvc.{ Result, RequestHeader }

import scala.concurrent.{ ExecutionContext, Future }

trait OAuthProviderUserController extends OAuthControllerBase { self: OptionalAuthElement with AuthConfig with OAuthProviderUserSupport =>

  override def gotoOAuthLoginSucceeded(token: AccessToken)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    retrieveProviderUser(token).flatMap(u => gotoOAuthLoginSucceededWithUser(u))
  }

  override def gotoOAuthLinkSucceeded(token: AccessToken, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    retrieveProviderUser(token).flatMap(gotoOAuthLinkSucceededWithUser(_, consumerUser))
  }

  def gotoOAuthLoginSucceededWithUser(providerUser: ProviderUser)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result]

  def gotoOAuthLinkSucceededWithUser(providerUser: ProviderUser, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result]

}
