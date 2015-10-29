package scroll.benchmarks.models

import scroll.internal.Compartment
import scroll.internal.annotations.Role

object SmallBank {

  class Account(custId: Int, name: String)

  class Bank extends Compartment {

    val transfer = Relationship("transfer").from[CheckingAccount](1).to[SavingsAccount](1)

    @Role
    class CheckingAccount(balance: Float)

    @Role
    class SavingsAccount(balance: Float)

  }

}