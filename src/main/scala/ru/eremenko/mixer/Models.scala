package ru.eremenko.mixer

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat, deserializationError}
import scala.util.Try

final case class Address(s: String)

final case class TransactionModel(
                                   timestamp: DateTime,
                                   fromAddress: String,
                                   toAddress: String,
                                   amount: String)

final case class Transaction(
                              timestamp: DateTime,
                              fromAddress: Address,
                              toAddress: Address,
                              amount: Double) {
  def toTransactionModel() : TransactionModel =
    TransactionModel(timestamp, fromAddress.s, toAddress.s, amount.toString)
}

object Transaction {
  def from(tm: TransactionModel) : Try[Transaction] = {
    Try(tm.amount.toDouble).
      map(Transaction(tm.timestamp, Address(tm.fromAddress), Address(tm.toAddress), _))
  }
}

final case class AddressInfoModel(balance: String, transactions: List[TransactionModel])


trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  val dateTimeParser: DateTimeFormatter = ISODateTimeFormat.dateTimeParser()

  implicit object DateTimeFormat extends RootJsonFormat[DateTime] {
    def write(c: DateTime): JsString = JsString(c.toString)

    def read(value: JsValue): DateTime = value match {
      case s: JsString => dateTimeParser.parseDateTime(s.value)
      case _ => deserializationError("Timestamp expected")
    }
  }

  implicit val addressFormatter: RootJsonFormat[Address] = jsonFormat1(Address)
  implicit val transactionFormatter: RootJsonFormat[TransactionModel] = jsonFormat4(TransactionModel)
  implicit val addressInfoFormatter: RootJsonFormat[AddressInfoModel] = jsonFormat2(AddressInfoModel)
}
