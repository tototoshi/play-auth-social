package com.github.tototoshi.play.social.core

import java.util.UUID

import jp.t2v.lab.play2.auth.{ AuthenticityToken, AuthConfig, OptionalAuthElement }
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

trait OAuth2Controller extends Controller { self: OptionalAuthElement with AuthConfig =>

  val authenticator: OAuth2Authenticator

  val OAUTH2_STATE_KEY = "play.auth.social.oauth2.state"

  def login(scope: String) = AsyncStack { implicit request =>
    loggedIn match {
      case Some(u) =>
        loginSucceeded(request)
      case None =>
        // should be more random ?
        val state = UUID.randomUUID().toString
        Future.successful(
          Redirect(authenticator.getAuthorizationUrl(scope, state)).withSession(
            OAUTH2_STATE_KEY -> state
          )
        )
    }
  }

  def link(scope: String) = AsyncStack { implicit request =>
    loggedIn match {
      case Some(u) =>
        // TODO should it be more random ?
        val state = UUID.randomUUID().toString
        Future.successful(
          Redirect(authenticator.getAuthorizationUrl(scope, state)).withSession(
            OAUTH2_STATE_KEY -> state
          )
        )
      case None =>
        Future.successful(Unauthorized)
    }
  }

  def authorize = Action.async { implicit request =>
    val form = Form(
      tuple(
        "code" -> nonEmptyText,
        "state" -> nonEmptyText.verifying { state =>
          state == request.session.get(OAUTH2_STATE_KEY).getOrElse("")
        }
      )
    )

    def isLoggedIn(token: AuthenticityToken): Future[Boolean] =
      idContainer.get(token).map(_.isDefined)

    form.bindFromRequest.fold({
      formWithError => Future.successful(BadRequest)
    }, {
      case (code, _) =>
        val action =
          tokenAccessor.extract(request).map { token =>
            isLoggedIn(token).map { loggedIn =>
              if (loggedIn) authenticator.gotoLinkSucceeded(_)
              else authenticator.gotoLoginSucceeded(_)
            }
          }.getOrElse {
            Future.successful(authenticator.gotoLoginSucceeded _)
          }

        (for {
          token <- authenticator.retrieveAccessToken(code)
          providerUser <- authenticator.retrieveProviderUser(token)
          _ = Logger.debug("Retrieve user info from oauth provider: " + providerUser.toString)
          a <- action
          result <- a(providerUser)
        } yield result).recover {
          case NonFatal(e) =>
            Logger.error(e.getMessage, e)
            InternalServerError
        }
    })
  }
}
