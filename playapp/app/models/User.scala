package models

import com.github.tototoshi.play.social.facebook.oauth2
import com.github.tototoshi.play.social.github
import com.github.tototoshi.play.social.twitter.oauth10a
import scalikejdbc._

sealed trait Authority
case object Admin extends Authority
case object Normal extends Authority

case class User(id: Long, name: String, avatarUrl: String)

case class GitHubUser(
  userId: Long,
  id: Long,
  login: String,
  avatarUrl: String,
  accessToken: String)

case class FacebookUser(
  userId: Long,
  id: String,
  name: String,
  coverUrl: String,
  accessToken: String)

case class TwitterUser(
  userId: Long,
  id: Long,
  screenName: String,
  profileImageUrl: String,
  accessToken: String,
  accessTokenSecret: String)

object User {

  def *(rs: WrappedResultSet) = User(
    rs.long("id"),
    rs.string("name"),
    rs.string("avatar_url")
  )

  def create(name: String, avatarUrl: String)(implicit session: DBSession): User = {
    DB.localTx { implicit session =>
      val id = sql"INSERT INTO users(name, avatar_url) VALUES ($name, $avatarUrl)".updateAndReturnGeneratedKey.apply()
      User(id, name, avatarUrl)
    }
  }

  def find(id: Long)(implicit session: DBSession): Option[User] = {
    DB.readOnly { implicit session =>
      sql"SELECT * FROM users WHERE id = $id".map(*).single().apply()
    }
  }

}

object GitHubUser {

  def *(rs: WrappedResultSet) = GitHubUser(
    rs.long("user_id"),
    rs.long("id"),
    rs.string("login"),
    rs.string("avatar_url"),
    rs.string("access_token")
  )

  def findById(id: Long)(implicit session: DBSession): Option[GitHubUser] = {
    sql"SELECT * FROM github_users WHERE id = $id".map(*).single().apply()
  }

  def findByUserId(userId: Long)(implicit session: DBSession): Option[GitHubUser] = {
    sql"SELECT * FROM github_users WHERE user_id = $userId".map(*).single().apply()
  }

  def save(userId: Long, gitHubUser: github.oauth2.GitHubUser)(implicit session: DBSession): GitHubUser = {
    val id = gitHubUser.id
    val login = gitHubUser.login
    val avatarUrl = gitHubUser.avatarUrl
    val accessToken = gitHubUser.accessToken
    sql"INSERT INTO github_users(user_id, id, login, avatar_url, access_token) VALUES ($userId, $id, $login, $avatarUrl, $accessToken)".update.apply()
    GitHubUser(userId, id, login, avatarUrl, accessToken)
  }

}

object FacebookUser {

  def *(rs: WrappedResultSet) = FacebookUser(
    rs.long("user_id"),
    rs.string("id"),
    rs.string("name"),
    rs.string("cover_url"),
    rs.string("access_token")
  )

  def findById(id: String)(implicit session: DBSession): Option[FacebookUser] = {
    sql"SELECT * FROM facebook_users WHERE id = $id".map(*).single().apply()
  }

  def findByUserId(userId: Long)(implicit session: DBSession): Option[FacebookUser] = {
    sql"SELECT * FROM facebook_users WHERE user_id = $userId".map(*).single().apply()
  }

  def save(userId: Long, facebookUser: oauth2.FacebookUser)(implicit session: DBSession): FacebookUser = {
    val id = facebookUser.id
    val name = facebookUser.name
    val coverUrl = facebookUser.coverUrl
    val accessToken = facebookUser.accessToken
    sql"INSERT INTO facebook_users(user_id, id, name, cover_url, access_token) VALUES ($userId, $id, $name, $coverUrl, $accessToken)".update.apply()
    FacebookUser(userId, id, name, coverUrl, accessToken)
  }

}

object TwitterUser {

  def *(rs: WrappedResultSet) = TwitterUser(
    rs.long("user_id"),
    rs.long("id"),
    rs.string("screen_name"),
    rs.string("profile_image_url"),
    rs.string("access_token"),
    rs.string("access_token_secret")
  )

  def findById(id: Long)(implicit session: DBSession): Option[TwitterUser] = {
    sql"SELECT * FROM twitter_users WHERE id = $id".map(*).single().apply()
  }

  def findByUserId(userId: Long)(implicit session: DBSession): Option[TwitterUser] = {
    sql"SELECT * FROM twitter_users WHERE user_id = $userId".map(*).single().apply()
  }

  def save(userId: Long, twitterUser: oauth10a.TwitterUser)(implicit session: DBSession): TwitterUser = {
    val id = twitterUser.id
    val screenName = twitterUser.screenName
    val profileImageUrl = twitterUser.profileImageUrl
    val accessToken = twitterUser.accessToken
    val accessTokenSecret = twitterUser.accessTokenSecret
    sql"""INSERT INTO twitter_users(user_id, id, screen_name, profile_image_url, access_token, access_token_secret)
          VALUES ($userId, $id, $screenName, $profileImageUrl, $accessToken, $accessTokenSecret)""".update.apply()
    TwitterUser(userId, id, screenName, profileImageUrl, accessToken, accessTokenSecret)
  }

}

