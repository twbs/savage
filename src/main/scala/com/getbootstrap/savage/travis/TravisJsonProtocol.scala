package com.getbootstrap.savage.travis

import spray.json._

object TravisJsonProtocol extends DefaultJsonProtocol {
  implicit val travisRepositoryFormat = jsonFormat2(Repository.apply)
  implicit val travisPayloadFormat = jsonFormat5(TravisPayload.apply)
}
