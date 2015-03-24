package controllers

import com.github.tototoshi.play.social.facebook.{ FacebookProviderUserSupport, FacebookOAuth2Controller }
import com.github.tototoshi.play.social.github.{ GitHubProviderUserSupport, GitHubOAuth2Controller }
import com.github.tototoshi.play.social.twitter.{ TwitterOAuth10aProviderUserSupport, TwitterOAuth10aController }
import jp.t2v.lab.play2.auth._
import models.{ FacebookUser, GitHubUser, TwitterUser, User }
import play.api.Logger
import play.api.mvc._
import scalikejdbc.DB

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.{ ClassTag, classTag }

import play.api.mvc.Results._

object Application extends Controller with OptionalAuthElement with AuthConfigImpl with Logout {

  def index = StackAction { implicit request =>
    DB.readOnly { implicit session =>
      val user = loggedIn
      val gitHubUser = user.flatMap(u => GitHubUser.findByUserId(u.id))
      val facebookUser = user.flatMap(u => FacebookUser.findByUserId(u.id))
      val twitterUser = user.flatMap(u => TwitterUser.findByUserId(u.id))
      Ok(views.html.index(user, gitHubUser, facebookUser, twitterUser))
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

object FacebookAuthController extends FacebookOAuth2Controller
    with AuthConfigImpl
    with FacebookProviderUserSupport {

  override def gotoLinkSucceeded(token: AccessToken, consumerUser: User)(implicit request: RequestHeader): Future[Result] = {
    for {
      providerUser <- retrieveProviderUser(token)
    } yield {
      DB.localTx { implicit session =>
        FacebookUser.save(consumerUser.id, providerUser)
        Redirect(routes.Application.index)
      }
    }
  }

  def gotoLoginSucceeded(providerUser: ProviderUser)(implicit request: RequestHeader): Future[Result] = {
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

  override def gotoLoginSucceeded(token: AccessToken)(implicit request: RequestHeader): Future[Result] = {
    for {
      providerUser <- retrieveProviderUser(token)
      result <- gotoLoginSucceeded(providerUser)
    } yield result
  }
}

object GitHubAuthController extends GitHubOAuth2Controller
    with AuthConfigImpl
    with GitHubProviderUserSupport {

  override def gotoLinkSucceeded(token: AccessToken, consumerUser: User)(implicit request: RequestHeader): Future[Result] = {
    for {
      providerUser <- retrieveProviderUser(token)
    } yield {
      DB.localTx { implicit session =>
        GitHubUser.save(consumerUser.id, providerUser)
        Redirect(routes.Application.index)
      }
    }
  }

  def gotoLoginSucceeded(providerUser: ProviderUser)(implicit request: RequestHeader): Future[Result] = {
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

  override def gotoLoginSucceeded(token: AccessToken)(implicit request: RequestHeader): Future[Result] = {
    for {
      providerUser <- retrieveProviderUser(token)
      result <- gotoLoginSucceeded(providerUser)
    } yield result
  }

}

object TwitterAuthController extends TwitterOAuth10aController
    with AuthConfigImpl
    with TwitterOAuth10aProviderUserSupport {

  override def gotoLinkSucceeded(accessToken: AccessToken, consumerUser: User)(implicit request: RequestHeader): Future[Result] = {
    for {
      providerUser <- retrieveProviderUser(authenticator.consumerKey, accessToken)
    } yield {
      DB.localTx { implicit session =>
        TwitterUser.save(consumerUser.id, providerUser)
        Redirect(routes.Application.index)
      }
    }
  }

  def gotoLoginSucceeded(providerUser: ProviderUser)(implicit request: RequestHeader): Future[Result] = {
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

  override def gotoLoginSucceeded(accessToken: AccessToken)(implicit request: RequestHeader): Future[Result] = {
    for {
      providerUser <- retrieveProviderUser(authenticator.consumerKey, accessToken)
      result <- gotoLoginSucceeded(providerUser)
    } yield result
  }

}
