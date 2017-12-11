package ru.eremenko.mixer

import org.scalactic.anyvals.{PosDouble, PosInt}
import org.scalatest.{FlatSpec, Matchers}

class SplitterSpec extends FlatSpec with Matchers {
  "EqSplitter" should "return List[PosDouble] - split amount on n equal parts" in {
    val amount = PosDouble(100.00)

    val split = EqSplitter.split(amount, PosInt(13))

    val sum = split.foldLeft(0.0)(_ + _)

    PosDouble.from(sum) shouldBe Some(amount)
  }
}
