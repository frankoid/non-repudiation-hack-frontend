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

import java.util.UUID

import play.api.test.WsTestClient
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class MultiChainServiceSpec extends UnitSpec with WsTestClient {

  "list" should {
    "list published events" in {
      withClient { wsClient =>
        val uniqueString = UUID.randomUUID().toString
        val multiChainService = new MultiChainService(wsClient, "http://localhost:4272", "multichainrpc", "CoxcdVSWVBP9TR52V3Kmddm8jvzmNHn1VPPWfca6dSQb", "streamX")

        await(multiChainService.publishEvent(Event(uniqueString)))

        val events: Seq[Event] = await(multiChainService.list())
        events should contain(Event(uniqueString))
      }
    }
  }

  "encode / decode hex" should {
    "round trip" in {
      import MultiChainService.{decodeHex, encodeHex}
      decodeHex(encodeHex("Hello world!")) shouldBe "Hello world!"
    }
  }
}
