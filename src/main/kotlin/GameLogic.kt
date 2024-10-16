import kotlin.random.Random

fun createGameField(rows: Int, cols: Int, fillRandomly: Boolean): Array<Array<Int?>> {
    val gameField = Array(rows) { arrayOfNulls<Int?>(cols) }
    if (fillRandomly) {
        for (i in gameField.indices) {
            for (j in gameField[i].indices) {
                val color: Int = Random.nextInt(4)
                if (color == 3)
                    gameField[i][j] = null
                else
                    gameField[i][j] = color
            }
        }
    }
    return gameField
}


fun countAliveNeighbors(gameField: Array<Array<Int?>>, row: Int, col: Int, color: Int): Int {
    var count = 0
    val rows = gameField.size
    val cols = gameField[0].size

    for (i in -1..1) {
        for (j in -1..1) {
            if (i == 0 && j == 0) {
                continue
            }
            val neighborRow = (row + i + rows) % rows
            val neighborCol = (col + j + cols) % cols

            if (gameField[neighborRow][neighborCol] == color) {
                count++
            }
        }
    }
    return count
}

@Synchronized
fun nextGeneration(gameField: Array<Array<Int?>>, survivalRule: List<Int>, birthRule: List<Int>): Array<Array<Int?>> {

    val rows = gameField.size
    val cols = gameField[0].size
    val newGameField = Array(rows) { arrayOfNulls<Int?>(cols) }

    for (i in 0 until rows) {
        for (j in 0 until cols) {
            val cellColor = gameField[i][j]
            val aliveNeighbors = cellColor?.let { countAliveNeighbors(gameField, i, j, it) }

            if (cellColor != null) {
                when (aliveNeighbors) {
                    in survivalRule -> newGameField[i][j] = cellColor
                    else -> newGameField[i][j] = null
                }
            } else {
                val zeroNeighborsColor = countAliveNeighbors(gameField, i, j, 0)
                val firstNeighborsColor = countAliveNeighbors(gameField, i, j, 1)
                val secondNeighborsColor = countAliveNeighbors(gameField, i, j, 2)
                when {
                    zeroNeighborsColor in birthRule -> {
                        newGameField[i][j] = 0
                    }

                    firstNeighborsColor in birthRule -> {
                        newGameField[i][j] = 1
                    }

                    secondNeighborsColor in birthRule -> {
                        newGameField[i][j] = 2
                    }

                    else -> {
                        newGameField[i][j] = null
                    }
                }
            }
        }
    }
    return newGameField
}
