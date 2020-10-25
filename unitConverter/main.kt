package converter

import java.util.Scanner
import converter.Type.*
import converter.Unit.*
import converter.States.*

enum class Type(val description: String) {
    LENGTH("Length"),
    WEIGHT("Weight"),
    TEMPERATURE("Temperature"),
    NULL("Unknown type");

    companion object {
        fun convert(value: Double, fromUnit: String, toUnit: String): String {
            val (fUnit, fType) = Unit.factory(fromUnit)
            val (tUnit, tType) = Unit.factory(toUnit)

            val result: Double
            when (fType to tType) {
                LENGTH to LENGTH, WEIGHT to WEIGHT -> {
                    if (value < 0.0) {
                        return "${fType.description} shouldn't be negative"
                    }

                    result = factorConvert(value, fUnit, tUnit)
                }
                TEMPERATURE to TEMPERATURE -> {
                    result = tempConvert(value, fUnit, tUnit)
                }
                else -> {
                    return "Conversion from ${fUnit.longName} to ${tUnit.longName} is impossible"
                }
            }
            return "$value ${convertName(value, fUnit)} is $result ${convertName(result, tUnit)}"
        }

        private fun factorConvert(value: Double, from: Unit, to: Unit): Double {
            return value * from.factor / to.factor
        }

        private fun tempConvert(value: Double, from: Unit, to: Unit): Double {
            if (from == to) return value
            return when (from to to) {
                CELSIUS to FAHRENHEIT -> value * (9.0 / 5.0) + 32
                FAHRENHEIT to CELSIUS -> (value - 32) * (5.0 / 9.0)
                KELVIN to CELSIUS -> value - 273.15
                CELSIUS to KELVIN -> value + 273.15
                FAHRENHEIT to KELVIN -> (value + 459.67) * (5.0 / 9.0)
                KELVIN to FAHRENHEIT -> value * (9.0 / 5.0) - 459.67
                else -> -1.0
            }
        }

        private fun convertName(value: Double, unit: Unit): String {
            return choose(unit, value == 1.0)
        }

        private fun choose(unit: Unit, singular: Boolean): String {
            return if (singular) unit.shortName else unit.longName
        }
    }
}

enum class Unit(val names: Array<String>, val shortName: String, val longName: String, val factor: Double, val type: Type) {
    METER(arrayOf("m"), "meter", "meters", 1.0, LENGTH),
    KILOMETER(arrayOf("km"), "kilometer", "kilometers", 1000.0, LENGTH),
    CENTIMETER(arrayOf("cm"), "centimeter", "centimeters", 0.01, LENGTH),
    MILLIMETER(arrayOf("mm"), "millimeter", "millimeters", 0.001, LENGTH),
    MILE(arrayOf("mi"), "mile", "miles", 1609.35, LENGTH),
    YARD(arrayOf("yd"), "yard", "yards", 0.9144, LENGTH),
    FOOT(arrayOf("ft"), "foot", "feet", 0.3048, LENGTH),
    INCH(arrayOf("in"), "inch", "inches", 0.0254, LENGTH),
    GRAM(arrayOf("g"), "gram", "grams", 1.0, WEIGHT),
    KILOGRAM(arrayOf("kg"), "kilogram", "kilograms", 1000.0, WEIGHT),
    MILLIGRAM(arrayOf("mg"), "milligram", "milligrams", 0.001, WEIGHT),
    POUND(arrayOf("lb"), "pound", "pounds", 453.592, WEIGHT),
    OUNCE(arrayOf("oz"), "ounce", "ounces", 28.349, WEIGHT),
    CELSIUS(arrayOf("celsius", "dc", "c"), "degree Celsius", "degrees Celsius", -1.0, TEMPERATURE),
    FAHRENHEIT(arrayOf("fahrenheit", "df", "f"), "degree Fahrenheit", "degrees Fahrenheit", -1.0, TEMPERATURE),
    KELVIN(arrayOf("k"), "Kelvin", "Kelvins", -1.0, TEMPERATURE),
    NULL(arrayOf(""), "???", "???", -1.0, Type.NULL);

    companion object {
        fun factory(nameIn: String): Pair<Unit, Type> {
            val name = nameIn.toLowerCase()
            for (enum in values()) {
                if (enum.names.contains(name) || enum.shortName.toLowerCase() == name || enum.longName.toLowerCase() == name) {
                    return (enum to enum.type)
                }
            }
            return (NULL to NULL.type)
        }
    }
}

enum class States(val prompt: String) {
    IDLE("Enter what you want to convert (or exit): "),
    READ_NUMBER(""),
    READ_UNIT_NAME_IN(""),
    READ_DEGREE_UNIT_NAME_IN(""),
    READ_FILLER_WORD(""),
    READ_DEGREE_UNIT_NAME_OUT(""),
    EXIT("");
}

data class Request(var fromValue: Double, var fromUnit: String, var toUnit: String) {
    fun reset() {
        fromValue = 0.0
        fromUnit = ""
        toUnit = ""
    }
}

object Parser {
    var state: States = IDLE

    private val request = Request(0.0, "", "")

    fun prompt() {
        if (state == IDLE) println(state.prompt)
    }

    fun hasMoreConversions(): Boolean {
        return state != States.EXIT
    }

    fun parse(input: String){
        state = when {
            (state == IDLE && input.toDoubleOrNull() != null) -> {
                request.fromValue = input.toDouble()
                READ_NUMBER
            }
            (state == READ_NUMBER && (input.toLowerCase() == "degree" || input.toLowerCase() == "degrees")) -> {
                request.fromUnit = input.toLowerCase()
                READ_DEGREE_UNIT_NAME_IN
            }
            (state == READ_NUMBER || state == READ_DEGREE_UNIT_NAME_IN) -> {
                request.fromUnit += (if (request.fromUnit != "") " " else "") + input.toLowerCase()
                READ_UNIT_NAME_IN
            }
            (state == READ_UNIT_NAME_IN) -> {
                READ_FILLER_WORD
            }
            (state == READ_FILLER_WORD && (input.toLowerCase() == "degree" || input.toLowerCase() == "degrees")) -> {
                request.toUnit = input.toLowerCase()
                READ_DEGREE_UNIT_NAME_OUT
            }
            (state == READ_FILLER_WORD || state == READ_DEGREE_UNIT_NAME_OUT) -> {
                request.toUnit += (if (request.toUnit != "") " " else "") + input.toLowerCase()
                println(Type.convert(request.fromValue, request.fromUnit, request.toUnit))
                request.reset()
                IDLE
            }
            (state == IDLE && input == "exit") -> {
                EXIT
            }
            else -> {
                println("Parse error")
                request.reset()
                IDLE
            }
        }
    }
}

fun main() {
    val scanner = Scanner(System.`in`)

    do {
        Parser.prompt()
        Parser.parse(scanner.next())
    } while (Parser.hasMoreConversions())
}
