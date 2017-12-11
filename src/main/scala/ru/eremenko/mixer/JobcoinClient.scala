package ru.eremenko.mixer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import cats.data.NonEmptyList
import spray.json._

import scala.concurrent.{ExecutionContext, Future}


class JobcoinClient(baseUrl: String)
                   (implicit system: ActorSystem,
                    m: ActorMaterializer,
                    ex: ExecutionContext) extends JsonSupport {
  def getAddressInfo(a: Address) : Future[AddressInfoModel] = {
    val addressInfoUri = s"$baseUrl/hurt/api/addresses/${a.s}"
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = addressInfoUri))
    responseFuture.flatMap(r => Unmarshal(r.entity).to[AddressInfoModel])
  }

  def getTransactions() : Future[List[TransactionModel]] = {
    val transactionsUri = s"$baseUrl/hurt/api/transactions"
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = transactionsUri))
    responseFuture.flatMap(r => Unmarshal(r.entity).to[List[TransactionModel]])
  }

  def postTransaction(t: TransactionModel) : Future[Boolean] = {
    val postTransactionsUri = s"$baseUrl/hurt/api/transactions"
    val transJson = t.toJson.toString()

    val entity = HttpEntity(ContentTypes.`application/json`, transJson)
    val request = HttpRequest(
      uri = postTransactionsUri,
      method = HttpMethods.POST,
      entity = entity
    )

    val responseFuture: Future[HttpResponse] = Http().singleRequest(request)

    responseFuture.map{ r =>
      val res = r.status == StatusCodes.OK
      if(!res) println(s"Transaction failed: ${r.entity}\n$TransactionModel\n")
      res
    }
  }

  def runTransactions( in: NonEmptyList[TransactionTuple]): Future[List[(Boolean, TransactionTuple)]] = {
    val res =
      in.map{ t =>
        val tm = t.transaction.toTransactionModel()
        this.postTransaction(tm).map((_, t))
      }

    Future.sequence(res.toList)
  }

  def sendToHomeAccount(mixerInputs: NonEmptyList[MixerInput],
                        homeAccount: Address) : Future[List[TransactionTuple]]= {
    val t = Mixer.transToHomeAccount(mixerInputs, homeAccount)
    this.runTransactions(t).map(_.filter(_._1).map(_._2))
  }

  def sendFromHomeAccount(allocator: MixerInput => NonEmptyList[Allocation])
                       (homeAccount: Address, mixerInputs: NonEmptyList[MixerInput]) : Future[List[TransactionTuple]] = {
    val t = Mixer.transFromHomeAccount(allocator)(homeAccount, mixerInputs)
    this.runTransactions(t).map(_.filter(_._1).map(_._2))
  }

  def mix(allocator: MixerInput => NonEmptyList[Allocation])
         (mixerInputs: NonEmptyList[MixerInput], homeAccount: Address): Future[List[TransactionTuple]] = {
    val r = sendToHomeAccount(mixerInputs, homeAccount)

    r.flatMap{ ts =>
      val in = NonEmptyList.fromList(ts.map(_.mixerInput))

      in match {
        case Some(m) => sendFromHomeAccount(allocator)(homeAccount, m)
        case None => Future(Nil)
      }
    }
  }
}
