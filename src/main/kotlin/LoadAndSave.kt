import java.io.File
import javax.swing.JOptionPane


fun showConfirmationDialog(): Int {
    val options = arrayOf("Да", "Нет")
    return JOptionPane.showOptionDialog(
        null,
        "Вы хотите сохранить состояние поля перед закрытием?",
        "Подтверждение",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]
    )
}

fun saveStateToFile(gameField: Array<Array<Int?>>, filePath: String) {
    val file = File(filePath)
    val stringBuilder = StringBuilder()

    for (row in gameField) {
        for (cell in row) {
            val value = cell?.toString() ?: "-1"
            stringBuilder.append(value)
            stringBuilder.append(" ")
        }
        stringBuilder.append("\n")
    }


    file.writeText(stringBuilder.toString())
}

fun loadStateFromFile(filePath: File): Array<Array<Int?>> {
    val lines = filePath.readLines()

    val loadGameField = Array(lines.size) { arrayOfNulls<Int?>(0) }

    for (i in lines.indices) {
        val line = lines[i].trim()
        val values = line.split(" ").toTypedArray()
        loadGameField[i] = Array(values.size) { j -> if (values[j].isNotEmpty() && values[j].toInt() >= 0) values[j].toInt() else null }
    }
    return loadGameField
}
