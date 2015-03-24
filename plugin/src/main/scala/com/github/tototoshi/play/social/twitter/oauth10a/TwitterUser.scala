package com.github.tototoshi.play.social.twitter.oauth10a

case class TwitterUser(
  id: Long,
  screenName: String,
  name: String,
  description: String,
  profileImageUrl: String,
  accessToken: String,
  accessTokenSecret: String)
