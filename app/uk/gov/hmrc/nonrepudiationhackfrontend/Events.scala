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
import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.nonrepudiation.MultiChainService
import uk.gov.hmrc.nonrepudiationhackfrontend.models.Event
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.util.control.NonFatal

/**
  * Created by max on 19/07/17.
  */
@Singleton
class Events @Inject() (multiChainService: MultiChainService) extends FrontendController{

  def getEvents = Action.async{ implicit request =>
    multiChainService.list().map {
      case Failure(throwable) => InternalServerError(throwable.getMessage)
      case Success(events) => Ok(Json.toJson(events))
    }.recover{
      case NonFatal(e) =>
        Logger.error(e.getMessage,e)
        InternalServerError(e.getMessage)
    }
  }

  def addEvent = Action.async{ implicit request =>
    val result = for {
      json <- (request.body.asJson toRight BadRequest).right
      event <- json.validate[Event].asOpt.toRight(BadRequest).right
    } yield multiChainService.publishEvent(event)

    result.fold(
      e => Future.successful(e),
      (eventualUnit: Future[Unit]) => eventualUnit.map(_ => Ok)
    )

  }

}
