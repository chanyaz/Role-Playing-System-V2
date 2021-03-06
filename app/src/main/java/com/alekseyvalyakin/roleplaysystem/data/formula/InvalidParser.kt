package com.alekseyvalyakin.roleplaysystem.data.formula

class InvalidParser(
        private val string: String = "x1"
) : FormulaPartParser {

    override fun parse(string: String): FormulaPart? {
        if (string == this.string) {
            throw IllegalArgumentException("Invalid pattern matched")
        }

        return null
    }
}