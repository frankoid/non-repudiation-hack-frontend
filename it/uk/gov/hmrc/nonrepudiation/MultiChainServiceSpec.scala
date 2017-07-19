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

import java.time.Month
import java.util.UUID

import org.scalatestplus.play.OneAppPerSuite
import play.api.test.WsTestClient
import uk.gov.hmrc.nonrepudiationhackfrontend.models.Event
import uk.gov.hmrc.play.test.UnitSpec

import scala.util.{Failure, Success}

class MultiChainServiceSpec extends UnitSpec with WsTestClient with OneAppPerSuite {

  val multiChainService = app.injector.instanceOf(classOf[MultiChainService])

  "list" should {
    "list published events" in {
      withClient { wsClient =>
        val uniqueString = UUID.randomUUID().toString

        await(multiChainService.publishEvent(Event(uniqueString,None)))

        await(multiChainService.list()) match {
          case Failure(e) =>
            fail(s"Failed: ${e.getMessage}")
          case Success(events) =>
            events.map(_.data) should contain(uniqueString)
            events.foreach { event =>
              event.timestamp should not be None
              // ensure time is being converted correcly - blocktime contains seconds not millis
              event.timestamp.get.getYear should be >= 2017
            }
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

  "blocktimeToLocalDateTime" should {
    "convert time correctly" in {
      val converted = multiChainService.blocktimeToLocalDateTime(1500462968)
      converted.getYear shouldBe 2017
      converted.getMonth shouldBe Month.JULY
      converted.getDayOfMonth shouldBe 19
    }
  }

}
