package machine

import java.util.Scanner

object CoffeeMachine {
    fun emptyCashier(): Int {
        return cashier.withdraw()
    }
    fun addSupplies(water: Int, milk: Int, beans: Int, cups: Int) {
        supplies = supplies.add(Supplies(water, milk, beans, cups))
    }
    fun sellCoffee(choice: Int): String {
        val item = Coffee.values()[choice]
        return makeCoffee(item)
    }
    override fun toString(): String {
        return "The coffee machine has:\n${supplies}\n${cashier}"
    }

    class Supplies(private val water: Int, private val milk: Int, private val beans: Int, private val cups: Int) {
        fun add(other: Supplies): Supplies {
            return Supplies(this.water + other.water, this.milk + other.milk, this.beans + other.beans, this.cups + other.cups)
        }

        fun remove(other: Supplies): Supplies {
            return Supplies(this.water - other.water, this.milk - other.milk, this.beans - other.beans, this.cups - other.cups)
        }

        fun checkEnough(required: Supplies): String {
            val remainingSupplies = this.remove(required)
            return when {
                remainingSupplies.water < 0 -> "water"
                remainingSupplies.milk < 0 -> "milk"
                remainingSupplies.beans < 0 -> "beans"
                remainingSupplies.cups < 0 -> "cups"
                else -> "enough"
            }
        }

        override fun toString(): String {
            return "$water of water\n$milk of milk\n$beans of coffee beans\n$cups of disposable cups"
        }
    }
    class Cashier(private var money: Int) {
        fun deposit(amount: Int) {
            this.money += amount
        }
        fun withdraw(): Int {
            val amount = money
            money = 0
            return amount
        }
        override fun toString(): String {
            return "$$money of money"
        }
    }
    enum class Coffee(val water: Int, val milk: Int, val beans: Int, val money: Int) {
        ESP(250, 0, 16, 4),
        LAT(350, 75, 20, 7),
        CAP(200, 100, 12, 6);
    }

    private var supplies = Supplies(400, 540, 120, 9)
    private var cashier = Cashier(550)

    private fun makeCoffee(variant: Coffee): String {

        val needed = Supplies(variant.water, variant.milk, variant.beans, 1)
        val status = supplies.checkEnough(needed)

        return if (status == "enough") {
            supplies = supplies.remove(needed)
            cashier.deposit(variant.money)
            "I have enough resources, making you a coffee!"
        } else {
            "Sorry, not enough $status!"
        }
    }
}

object Handler {
    fun prompt() {
        println(state.prompt)
    }
    fun hasMoreJobs(): Boolean {
        return state != States.EXIT
    }
    fun handle(input: String) {
        state = when {
            (state == States.IDLE && input == "remaining") -> {
                println(CoffeeMachine.toString())
                States.IDLE
            }
            (state == States.IDLE && input == "take") -> {
                println("I gave you $${CoffeeMachine.emptyCashier()}")
                States.IDLE
            }
            (state == States.IDLE && input == "fill") -> {
                States.FILLING_WATER
            }
            (state == States.FILLING_WATER) -> {
                CoffeeMachine.addSupplies(input.toInt(),0, 0, 0)
                States.FILLING_MILK
            }
            (state == States.FILLING_MILK) -> {
                CoffeeMachine.addSupplies(0, input.toInt(),0, 0)
                States.FILLING_BEANS
            }
            (state == States.FILLING_BEANS) -> {
                CoffeeMachine.addSupplies(0, 0, input.toInt(),0)
                States.FILLING_CUPS
            }
            (state == States.FILLING_CUPS) -> {
                CoffeeMachine.addSupplies(0, 0, 0, input.toInt())
                States.IDLE
            }
            (state == States.IDLE && input == "buy") -> {
                States.SELLING
            }
            (state == States.SELLING && input in "123") -> {
                println(CoffeeMachine.sellCoffee(input.toInt().dec()))
                States.IDLE
            }
            (state == States.SELLING && input == "back") -> {
                States.IDLE
            }
            (state == States.IDLE && input == "exit") -> {
                States.EXIT
            }
            else -> {
                println("Unrecognised input")
                States.IDLE
            }
        }
    }

    enum class States(val prompt: String) {
        IDLE("Write action (buy, fill, take, remaining, exit): > "),
        SELLING("What do you want to buy? 1 - espresso, 2 - latte, 3 - cappuccino, back - to main menu: > "),
        FILLING_WATER("Write how many ml of water do you want to add: > "),
        FILLING_MILK("Write how many ml of milk do you want to add: > "),
        FILLING_BEANS("Write how many grams of coffee beans do you want to add: > "),
        FILLING_CUPS("Write how many disposable cups of coffee do you want to add: > "),
        EXIT("Shutdown the machine");

    }

    var state: States = States.IDLE
}

fun main() {
    val scanner = Scanner(System.`in`)

    do {
        Handler.prompt()
        Handler.handle(scanner.next())
    } while (Handler.hasMoreJobs())
}