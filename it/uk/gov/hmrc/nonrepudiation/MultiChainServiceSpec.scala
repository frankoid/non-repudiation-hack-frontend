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

import org.scalatest.concurrent.Eventually
import play.api.test.WsTestClient
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class MultiChainServiceSpec extends UnitSpec with WsTestClient with Eventually {
  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = 10 seconds)

//  "publishEvent" should {
//    "publish event to the blockhain" in {
//      val multiChainService = new MultiChainService()
//      multiChainService.publishEvent(Event("Hello world!"))
//    }
//  }

  "list" should {
    "list published events" in {
      withClient { wsClient =>
        val uniqueString = UUID.randomUUID().toString
//        val uniqueString = "c0ffeecafe2"
//        val uniqueString = "c0ffeecafe3"
//        val uniqueString = "not really unique"
        val multiChainService = new MultiChainService(wsClient, "http://localhost:4272", "multichainrpc", "CoxcdVSWVBP9TR52V3Kmddm8jvzmNHn1VPPWfca6dSQb", "stream1")
        await(multiChainService.publishEvent(Event(uniqueString)))
        eventually {
          val events: Seq[Event] = await(multiChainService.list())
          events should contain(Event(uniqueString))
        }
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
