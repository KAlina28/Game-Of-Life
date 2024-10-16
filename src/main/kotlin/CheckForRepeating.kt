import java.util.*
import javax.swing.JOptionPane

val positionHistory = mutableListOf<Array<Array<Int?>>>()

fun saveAndCheckForRepetition(gameField: Array<Array<Int?>>, directory: String): Boolean {
    // Проверка повторения позиции
    if (positionHistory.any { Arrays.deepEquals(it, gameField) }) {
        JOptionPane.showMessageDialog(frame, "The game has entered a loop!")
        positionHistory.clear()
        return true
    }

    // Сохранение поля в файл
    val fileName = "position_${positionHistory.size}.txt"
    val filePath = "$directory/$fileName"
    saveStateToFile(gameField, filePath)

    // Добавление позиции в историю
    positionHistory.add(gameField.deepCopy())

    return false
}

fun Array<Array<Int?>>.deepCopy(): Array<Array<Int?>> {
    val copy = Array(size) { row ->
        Array(this[row].size) { col ->
            this[row][col]
        }
    }
    return copy
}
