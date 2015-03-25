package controllers

import com.github.tototoshi.play.social.core.OAuthProviderUserController
import com.github.tototoshi.play.social.facebook.oauth2.{ FacebookController, FacebookProviderUserSupport }
import com.github.tototoshi.play.social.github.oauth2.{ GitHubController, GitHubProviderUserSupport }
import com.github.tototoshi.play.social.slack.oauth2.SlackController
import com.github.tototoshi.play.social.twitter.oauth10a.{ TwitterController, TwitterProviderUserSupport }
import jp.t2v.lab.play2.auth._
import models._
import play.api.mvc.Results._
import play.api.mvc._
import scalikejdbc.DB

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.{ ClassTag, classTag }

object Application extends Controller with OptionalAuthElement with AuthConfigImpl with Logout {

  def index = StackAction { implicit request =>
    DB.readOnly { implicit session =>
      val user = loggedIn
      val gitHubUser = user.flatMap(u => GitHubUser.findByUserId(u.id))
      val facebookUser = user.flatMap(u => FacebookUser.findByUserId(u.id))
      val twitterUser = user.flatMap(u => TwitterUser.findByUserId(u.id))
      val slackAccessToken = user.flatMap(u => SlackAccessToken.findByUserId(u.id))
      Ok(views.html.index(user, gitHubUser, facebookUser, twitterUser, slackAccessToken))
    }
  }

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded
  }

}

trait AuthConfigImpl extends AuthConfig {
  type Id = Long
  type User = models.User
  type Authority = models.Authority
  val idTag: ClassTag[Id] = classTag[Id]
  val sessionTimeoutInSeconds: Int = 3600

  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] =
    Future.successful(DB.readOnly { implicit session =>
      User.find(id)
    })

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.index()))

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.index))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.index))

  def authorizationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Forbidden("no permission"))

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    true
  }

  override lazy val cookieSecureOption: Boolean = play.api.Play.isProd(play.api.Play.current)

}

object FacebookAuthController extends FacebookController
    with AuthConfigImpl
    with FacebookProviderUserSupport
    with OAuthProviderUserController {

  override def gotoOAuthLinkSucceededWithUser(providerUser: ProviderUser, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    Future.successful {
      DB.localTx { implicit session =>
        FacebookUser.save(consumerUser.id, providerUser)
        Redirect(routes.Application.index)
      }
    }
  }

  override def gotoOAuthLoginSucceededWithUser(providerUser: ProviderUser)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    DB.localTx { implicit session =>
      FacebookUser.findById(providerUser.id) match {
        case None =>
          val id = User.create(providerUser.name, providerUser.coverUrl).id
          FacebookUser.save(id, providerUser)
          gotoLoginSucceeded(id)
        case Some(fu) =>
          gotoLoginSucceeded(fu.userId)
      }
    }
  }

}

object GitHubAuthController extends GitHubController
    with AuthConfigImpl
    with GitHubProviderUserSupport
    with OAuthProviderUserController {

  override def gotoOAuthLinkSucceededWithUser(providerUser: ProviderUser, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    Future.successful {
      DB.localTx { implicit session =>
        GitHubUser.save(consumerUser.id, providerUser)
        Redirect(routes.Application.index)
      }
    }
  }

  override def gotoOAuthLoginSucceededWithUser(providerUser: ProviderUser)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    DB.localTx { implicit session =>
      GitHubUser.findById(providerUser.id) match {
        case None =>
          val id = User.create(providerUser.login, providerUser.avatarUrl).id
          GitHubUser.save(id, providerUser)
          gotoLoginSucceeded(id)
        case Some(gh) =>
          gotoLoginSucceeded(gh.userId)
      }
    }
  }

}

object TwitterAuthController extends TwitterController
    with AuthConfigImpl
    with TwitterProviderUserSupport
    with OAuthProviderUserController {

  override def gotoOAuthLinkSucceededWithUser(providerUser: ProviderUser, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    Future.successful {
      DB.localTx { implicit session =>
        TwitterUser.save(consumerUser.id, providerUser)
        Redirect(routes.Application.index)
      }
    }
  }

  override def gotoOAuthLoginSucceededWithUser(providerUser: ProviderUser)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    DB.localTx { implicit session =>
      TwitterUser.findById(providerUser.id) match {
        case None =>
          val id = User.create(providerUser.screenName, providerUser.profileImageUrl).id
          TwitterUser.save(id, providerUser)
          gotoLoginSucceeded(id)
        case Some(tu) =>
          gotoLoginSucceeded(tu.userId)
      }
    }
  }

}

object SlackAuthController extends SlackController
    with AuthConfigImpl {

  override def gotoOAuthLinkSucceeded(accessToken: AccessToken, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    Future.successful {
      DB.localTx { implicit session =>
        SlackAccessToken.save(consumerUser.id, accessToken)
        Redirect(routes.Application.index)
      }
    }
  }

  override def gotoOAuthLoginSucceeded(accessToken: AccessToken)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    ???
  }

}
