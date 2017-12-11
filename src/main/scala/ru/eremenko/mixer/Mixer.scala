package ru.eremenko.mixer

import org.joda.time.DateTime
import org.scalactic.anyvals._
import cats.data.NonEmptyList

final case class MixerInput(amount: PosDouble, source: Address, dist: NonEmptyList[Address])
final case class MixingFee(percent: PosDouble, address: Address)
final case class Allocation(address: Address, amount: PosDouble)
final case class TransactionTuple(transaction: Transaction, mixerInput: MixerInput)


object EqSplitter {
  def split(amount: PosDouble, n: PosInt): NonEmptyList[PosDouble] = {
    val a = PosDouble.from(amount/n).get
    NonEmptyList.fromListUnsafe(List.fill(n)(a))
  }
}

object Mixer {
  def allocate(splitter: (PosDouble, PosInt) => NonEmptyList[PosDouble])
              (mi: MixerInput) : NonEmptyList[Allocation] = {
    val size = PosInt.from(mi.dist.size).get
    val split = splitter(mi.amount, size)
    split.zipWith(mi.dist)((a, b) => Allocation(b, a))
  }

  def transToHomeAccount(mixerInputs: NonEmptyList[MixerInput],
                         homeAccount: Address): NonEmptyList[TransactionTuple] = {
    mixerInputs.map(m => TransactionTuple(Transaction(DateTime.now(), m.source, homeAccount, m.amount), m))
  }

  def transFromHomeAccount(allocator: MixerInput => NonEmptyList[Allocation])
                          (homeAccount: Address, mixerInputs: NonEmptyList[MixerInput]): NonEmptyList[TransactionTuple] = {
    mixerInputs.flatMap{ m =>
      allocator(m).map{ a =>
        TransactionTuple(Transaction(DateTime.now(), homeAccount, a.address, a.amount), m)
      }
    }
  }
}
