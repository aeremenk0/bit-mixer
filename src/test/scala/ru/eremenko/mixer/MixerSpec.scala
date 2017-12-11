package ru.eremenko.mixer

import cats.data.NonEmptyList
import org.scalactic.anyvals.PosDouble
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures


class MixerSpec extends FlatSpec with Matchers with ScalaFutures {
  def getMixerInputList(): (List[PosDouble], NonEmptyList[MixerInput]) = {
    val amountList = List(PosDouble(1.0), PosDouble(2.0), PosDouble(2.5))
    val amount1 = amountList(0)
    val amount2 = amountList(1)
    val amount3 = amountList(2)

    val m1 = MixerInput(amount1, Address("Test1"), NonEmptyList(Address("Test1Dist1"), List(Address("Test1Dist2"), Address("Test1Dist3"))))
    val m2 = MixerInput(amount2, Address("Test2"), NonEmptyList(Address("Test2Dist1"), List(Address("Test2Dist2"), Address("Test2Dist3"))))
    val m3 = MixerInput(amount3, Address("Test3"), NonEmptyList(Address("Test3Dist1"), List(Address("Test3Dist2"), Address("Test3Dist3"))))

    (amountList, NonEmptyList(m1, List(m2, m3)))
  }

  "Mixer.allocate" should "return List of Allocations" in {
    val amount = PosDouble(100.00)
    val dist = NonEmptyList(
      Address("AliceDist1"),
      List(Address("AliceDist2"), Address("AliceDist3"), Address("AliceDist4"))
    )

    val mixerInput = MixerInput(amount, Address("Alice"), dist)
    val alc = Mixer.allocate(EqSplitter.split)(mixerInput)

    val sum = alc.foldLeft(0.0)( _ + _.amount)
    PosDouble.from(sum) shouldBe Some(amount)
  }

  "Mixer.transToHomeAccount" should "generate Transactions to move money to home account" in {
    val (amountList, ms) = getMixerInputList()
    val res = Mixer.transToHomeAccount(ms, Address("HomeAccount"))

    res.foldLeft(0.0)( _ + _.transaction.amount ) shouldBe amountList.foldLeft(0.0)( _ + _ )
  }

  "Mixer.transToHomeAccount" should "generate Transactions to disperse money from home account" in {
    val (amountList, ms) = getMixerInputList()
    val res = Mixer.transFromHomeAccount(Mixer.allocate(EqSplitter.split))(Address("HomeAccount"), ms)

    val transAmount = res.foldLeft(0.0)( _ + _.transaction.amount )
    val expectedAmount = amountList.foldLeft(0.0)( _ + _ )

    (transAmount - expectedAmount) < 0.0000000001 shouldBe true
  }
}
