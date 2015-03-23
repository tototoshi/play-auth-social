package controllers

import com.github.tototoshi.play.social.facebook.{FacebookOAuth2Authenticator, FacebookOAuth2Controller}
import com.github.tototoshi.play.social.github.{GitHubOAuth2Authenticator, GitHubOAuth2Controller}
import com.github.tototoshi.play.social.twitter.{TwitterOAuth10aAuthenticator, TwitterOAuth10aController}
import jp.t2v.lab.play2.auth._
import models.{FacebookUser, GitHubUser, TwitterUser, User}
import play.api.mvc._
import scalikejdbc.DB

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.{ClassTag, classTag}

object Application extends Controller with OptionalAuthElement with AuthConfigImpl with LoginLogout {

  def index = StackAction { implicit request =>
    DB.readOnly { implicit session =>
      val user: Option[Application.User] = loggedIn
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
  self: Login =>

  import play.api.mvc.Results._

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

object FacebookAuthController extends FacebookOAuth2Controller with AuthConfigImpl {
  self =>
  lazy val authenticator = new FacebookOAuth2Authenticator {
    override def gotoLinkSucceeded(providerUser: ProviderUser)(implicit request: RequestHeader): Future[Result] = {
      tokenAccessor.extract(request) match {
        case None => Future.successful(Unauthorized)
        case Some(token) =>
          for {
            id <- idContainer.get(token)
          } yield {
            id.map { i =>
              DB.localTx { implicit session =>
                FacebookUser.save(i, providerUser)
                Redirect(routes.Application.index)
              }
            }.getOrElse {
              Unauthorized
            }
          }
      }
    }

    override def gotoLoginSucceeded(providerUser: ProviderUser)(implicit request: RequestHeader): Future[Result] = {
      DB.localTx { implicit session =>
        FacebookUser.findById(providerUser.id) match {
          case None =>
            val id = User.create(providerUser.name, providerUser.coverUrl).id
            FacebookUser.save(id, providerUser)
            self.gotoLoginSucceeded(id)
          case Some(fu) =>
            self.gotoLoginSucceeded(fu.userId)
        }
      }
    }
  }
}

object GitHubAuthController extends GitHubOAuth2Controller with AuthConfigImpl {
  self =>

  lazy val authenticator = new GitHubOAuth2Authenticator {
    override def gotoLinkSucceeded(providerUser: ProviderUser)(implicit request: RequestHeader): Future[Result] = {
      tokenAccessor.extract(request) match {
        case None => Future.successful(Unauthorized)
        case Some(token) =>
          for {
            id <- idContainer.get(token)
          } yield {
            id.map { i =>
              DB.localTx { implicit session =>
                GitHubUser.save(i, providerUser)
                Redirect(routes.Application.index)
              }
            }.getOrElse {
              Unauthorized
            }
          }
      }
    }

    override def gotoLoginSucceeded(providerUser: ProviderUser)(implicit request: RequestHeader): Future[Result] = {
      DB.localTx { implicit session =>
        GitHubUser.findById(providerUser.id) match {
          case None =>
            val id = User.create(providerUser.login, providerUser.avatarUrl).id
            GitHubUser.save(id, providerUser)
            self.gotoLoginSucceeded(id)
          case Some(gu) =>
            self.gotoLoginSucceeded(gu.userId)
        }
      }
    }
  }
}

object TwitterAuthController extends TwitterOAuth10aController with AuthConfigImpl {
  self =>
  lazy val authenticator = new TwitterOAuth10aAuthenticator {
    override def gotoLinkSucceeded(providerUser: ProviderUser)(implicit request: RequestHeader): Future[Result] = {
      tokenAccessor.extract(request) match {
        case None => Future.successful(Unauthorized)
        case Some(token) =>
          for {
            id <- idContainer.get(token)
          } yield {
            id.map { i =>
              DB.localTx { implicit session =>
                TwitterUser.save(i, providerUser)
                Redirect(routes.Application.index)
              }
            }.getOrElse {
              Unauthorized
            }
          }
      }
    }

    override def gotoLoginSucceeded(providerUser: ProviderUser)(implicit request: RequestHeader): Future[Result] = {
      DB.localTx { implicit session =>
        TwitterUser.findById(providerUser.id) match {
          case None =>
            val id = User.create(providerUser.screenName, providerUser.profileImageUrl).id
            TwitterUser.save(id, providerUser)
            self.gotoLoginSucceeded(id)
          case Some(tu) =>
            self.gotoLoginSucceeded(tu.userId)
        }
      }
    }
  }
}
