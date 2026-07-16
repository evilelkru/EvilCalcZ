package com.evilcalc.app

object SimpleEvaluator {

    fun eval(expression: String): Double {
        val tokens = tokenize(expression)
        val result = parseExpression(tokens, intArrayOf(0))
        return result
    }

    private data class Token(val type: TokenType, val value: Double = 0.0)
    private enum class TokenType { NUMBER, PLUS, MINUS, MUL, DIV, LPAREN, RPAREN }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < expr.length) {
            when {
                expr[i].isDigit() || expr[i] == '.' -> {
                    var num = ""
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        num += expr[i++]
                    }
                    tokens.add(Token(TokenType.NUMBER, num.toDouble()))
                }
                expr[i] == '+' -> { tokens.add(Token(TokenType.PLUS)); i++ }
                expr[i] == '-' -> { tokens.add(Token(TokenType.MINUS)); i++ }
                expr[i] == '*' -> { tokens.add(Token(TokenType.MUL)); i++ }
                expr[i] == '/' -> { tokens.add(Token(TokenType.DIV)); i++ }
                expr[i] == '(' -> { tokens.add(Token(TokenType.LPAREN)); i++ }
                expr[i] == ')' -> { tokens.add(Token(TokenType.RPAREN)); i++ }
                expr[i] == ' ' -> i++
                else -> throw IllegalArgumentException("Unknown char: ${expr[i]}")
            }
        }
        return tokens
    }

    private fun parseExpression(tokens: List<Token>, pos: IntArray): Double {
        var result = parseTerm(tokens, pos)
        while (pos[0] < tokens.size) {
            when (tokens[pos[0]].type) {
                TokenType.PLUS -> { pos[0]++; result += parseTerm(tokens, pos) }
                TokenType.MINUS -> { pos[0]++; result -= parseTerm(tokens, pos) }
                else -> break
            }
        }
        return result
    }

    private fun parseTerm(tokens: List<Token>, pos: IntArray): Double {
        var result = parseFactor(tokens, pos)
        while (pos[0] < tokens.size) {
            when (tokens[pos[0]].type) {
                TokenType.MUL -> { pos[0]++; result *= parseFactor(tokens, pos) }
                TokenType.DIV -> { pos[0]++; result /= parseFactor(tokens, pos) }
                else -> break
            }
        }
        return result
    }

    private fun parseFactor(tokens: List<Token>, pos: IntArray): Double {
        val token = tokens[pos[0]]
        return when (token.type) {
            TokenType.NUMBER -> { pos[0]++; token.value }
            TokenType.MINUS -> { pos[0]++; -parseFactor(tokens, pos) }
            TokenType.PLUS -> { pos[0]++; parseFactor(tokens, pos) }
            TokenType.LPAREN -> {
                pos[0]++
                val result = parseExpression(tokens, pos)
                if (pos[0] < tokens.size && tokens[pos[0]].type == TokenType.RPAREN) pos[0]++
                result
            }
            else -> throw IllegalArgumentException("Unexpected token: $token")
        }
    }
}
