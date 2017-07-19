/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.nonrepudiationhackfrontend

import java.time.LocalDateTime

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.nonrepudiationhackfrontend.models.Event
import uk.gov.hmrc.play.frontend.controller.FrontendController
import scala.concurrent.duration._

import scala.concurrent.Future

/**
  * Created by max on 19/07/17.
  */
class Events extends FrontendController{

  def getEvents = Action.async{ implicit request =>
    Logger.info("Getting Events")
    Future.successful(
      Ok(Json.toJson(List(
        Event("key","mydata",Some(LocalDateTime.now().minusDays(4))),
        Event("key2","mydata2",Some(LocalDateTime.now().minusDays(3))),
        Event("key","mydata3",Some(LocalDateTime.now().minusMinutes(132))),
        Event("key3","mydata4",Some(LocalDateTime.now()))
      )))
    )
  }

  def addEvent = Action.async{ implicit request =>
    Logger.info(s"Adding event: ${request.body}")
    Future.successful(Ok)

  }

}
