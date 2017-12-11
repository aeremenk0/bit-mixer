package ru.eremenko.mixer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.data.NonEmptyList
import org.scalactic.anyvals.PosDouble

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}


object Main extends JsonSupport {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val baseUrl = "http://jobcoin.gemini.com"

    val client = new JobcoinClient(baseUrl)


    val responseFuture = client.mix(Mixer.allocate(EqSplitter.split))(mixerInput(), Address("HomeAccount"))

    responseFuture
      .onComplete {
        case Success(res) => println(res)
        case Failure(ex)   => println(ex.getMessage)
      }

    Await.ready(responseFuture, 60 seconds)
  }

  def mixerInput() = {
    val amountList = List(PosDouble(1.0), PosDouble(2.0), PosDouble(2.5))
    val amount1 = amountList(0)
    val amount2 = amountList(1)
    val amount3 = amountList(2)

    val m1 = MixerInput(amount1, Address("Test1"), NonEmptyList(Address("Test1Dist1"), List(Address("Test1Dist2"), Address("Test1Dist3"))))
    val m2 = MixerInput(amount2, Address("Test2"), NonEmptyList(Address("Test2Dist1"), List(Address("Test2Dist2"), Address("Test2Dist3"))))
    val m3 = MixerInput(amount3, Address("Test3"), NonEmptyList(Address("Test3Dist1"), List(Address("Test3Dist2"), Address("Test3Dist3"))))

    NonEmptyList(m1, List(m2, m3))
  }
}