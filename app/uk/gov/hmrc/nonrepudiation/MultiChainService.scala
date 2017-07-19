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
import javax.inject.{Inject, Singleton}

import org.apache.commons.codec.binary.Hex
import play.api.libs.json.Json
import play.api.libs.ws.{WSAuthScheme, WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MultiChainService @Inject()(ws: WSClient, url: String, username: String, password: String)(implicit ec: ExecutionContext) {

  // we intend that the streamName will become and argument to list() and publishEvent later
  val streamName = "streamX"

  def list(): Future[Seq[Event]] =
    jsonRpc("liststreamitems", List(streamName)).map { resp =>
      // asOpt[String] skips results where the data is not a simple string but rather a large object
      // could be enhanced to go and fetch the large objects instead of skipping them
      val dataHexes = (resp.json \ "result" \\ "data").flatMap(_.asOpt[String])
      val datas = dataHexes.map(MultiChainService.decodeHex)
      datas.map(Event)
    }

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
