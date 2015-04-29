package com.github.tototoshi.play.social.facebook.oauth2

case class FacebookUser(
  id: String,
  name: String,
  email: String,
  coverUrl: String,
  accessToken: String)
