package com.getbootstrap.savage.travis

import spray.json._

object TravisJsonProtocol extends DefaultJsonProtocol {
  implicit val travisPayloadFormat = jsonFormat4(TravisPayload.apply)
}
