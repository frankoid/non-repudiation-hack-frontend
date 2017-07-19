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

package uk.gov.hmrc.nonrepudiation

import java.nio.charset.StandardCharsets
import java.time.{Instant, LocalDateTime, ZoneOffset}
import javax.inject.{Inject, Singleton}

import org.apache.commons.codec.binary.Hex
import play.api.Configuration
import play.api.libs.json.{JsArray, Json}
import play.api.libs.ws.{WSAuthScheme, WSClient, WSResponse}
import uk.gov.hmrc.nonrepudiationhackfrontend.models.Event

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class MultiChainService @Inject()(ws: WSClient,config:Configuration)(implicit ec: ExecutionContext) {

  val url: String = config.underlying.getString("multichain.url")
  val username: String= config.underlying.getString("multichain.username")
  val password: String = config.underlying.getString("multichain.password")

  // we intend that the streamName will become and argument to list() and publishEvent later
  val streamName = "streamX"

  def list(): Future[Try[Seq[Event]]] =
    jsonRpc("liststreamitems", List(streamName)).map { resp =>

      Try(
        (resp.json \ "result").as[JsArray].value.flatMap {
          jsVal =>
            (jsVal \ "data").asOpt[String].map(encodedData => Event(
              data = MultiChainService.decodeHex(encodedData),
              timestamp = Some(blocktimeToLocalDateTime((jsVal \ "blocktime").as[Long]))))
        }
      )

    }

  private[nonrepudiation] def blocktimeToLocalDateTime(blocktime: Long): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(blocktime), ZoneOffset.UTC)

  private def jsonRpc(method: String, parameters: List[String]): Future[WSResponse] = {
    ws.url(url)
      .withAuth(username, password, WSAuthScheme.BASIC)
      .post(Json.obj(
        "jsonrpc" -> "1.0",
        "id" -> "test",
        "method" -> method,
        "params" -> parameters
      ))
  }

  def publishEvent(event: Event): Future[Unit] =
    jsonRpc("publish", List(streamName, "key", MultiChainService.encodeHex(event.data))).map { resp =>
      if (resp.status == 200)
        ()
      else
        throw new IllegalStateException(s"${resp.status} ${resp.statusText}")
    }
}


object MultiChainService {

  def encodeHex(unencoded: String): String =
    Hex.encodeHexString(unencoded.getBytes(StandardCharsets.UTF_8))

  def decodeHex(encoded: String): String =
    new String(Hex.decodeHex(encoded.toCharArray), StandardCharsets.UTF_8)

}
