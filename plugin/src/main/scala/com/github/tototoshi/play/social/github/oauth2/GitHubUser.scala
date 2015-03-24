package com.github.tototoshi.play.social.github.oauth2

case class GitHubUser(
  id: Long,
  login: String,
  avatarUrl: String,
  accessToken: String)
