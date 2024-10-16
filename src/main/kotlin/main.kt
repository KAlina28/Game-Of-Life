import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.system.exitProcess


val frame = JFrame("Game of Life")

fun main() {
    val screenWidth = 800
    val screenHeight = 800

    val gameView = GameView(screenWidth, screenHeight)

    gameView.isFocusable = true
    frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    frame.setSize(screenWidth, screenHeight)

    //работа с правилами
    val survivalField = JTextField(10)
    val birthField = JTextField(10)
    //

    //file
    val fileChooser = JFileChooser()
    fileChooser.fileFilter = FileNameExtensionFilter("Text Files", "txt")

    val saveFolderPath = "saved_positions"
    val saveFolder = File(saveFolderPath)
    if (!saveFolder.exists()) {
        saveFolder.mkdirs()
    }




    //пауза
    val paused = AtomicBoolean(false) // Переменная для отслеживания состояния паузы
    gameView.requestFocus()

    var gameField = Array(20) { arrayOfNulls<Int?>(20) }


    SwingUtilities.invokeLater {

        val generateButton = JButton("Generate")
        val startButton = JButton("Start")
        val fillCheckBox = JCheckBox("Fill field randomly")
        val stopButton = JButton("Stop")
        val pauseButton = JButton("Pause")
        val stepButton = JButton("Step")
        val autoButton = JButton("Auto")
        val saveButton = JButton("Save")
        val loadButton = JButton("Load")


        val sizeField = JTextField(10)
        stopButton.isEnabled = false
        pauseButton.isEnabled = false

        //правила
        val survivalRuleInput = survivalField.text
        val survivalRule = if (survivalRuleInput.isNotEmpty()) {
            survivalRuleInput.split("[,\\s]+").filter { it.isNotEmpty() }.map { it.toInt() }
        } else {
            listOf(2, 3)
        }

        val birthRuleInput = birthField.text
        val birthRule = if (birthRuleInput.isNotEmpty()) {
            birthRuleInput.split("[,\\s]+").filter { it.isNotEmpty() }.map { it.toInt() }
        } else {
            listOf(3)
        }
        //


        var worker: SwingWorker<Array<Array<Int?>>, Any>? = null
        val rowsField = JTextField(10)
        val colsField = JTextField(10)
        val turnCountField = JTextField(10) // Поле для ввода количества ходов







        generateButton.addActionListener {

            val rows = rowsField.text.toIntOrNull() ?: 20
            val cols = colsField.text.toIntOrNull() ?: 20


            val fillRandomly = fillCheckBox.isSelected


            gameField = createGameField(rows, cols, fillRandomly)
            gameView.isFocusable = true

            gameView.setGameField(gameField)
            gameView.setEditable(true)
            gameView.clearInfoGeneration()
            gameView.updateInfoGeneration()
            gameView.repaint()


            startButton.isEnabled = true
        }

        startButton.isEnabled = false

        startButton.addActionListener(object : ActionListener {
            override fun actionPerformed(e: ActionEvent) {

                if (!generateButton.isEnabled) {
                    return
                }
                gameField = gameView.getGameField()!!
                gameView.setEditable(false)
                startButton.isEnabled = false
                pauseButton.isEnabled = true
                stopButton.isEnabled = true
                generateButton.isEnabled = false
                sizeField.isEnabled = false
                fillCheckBox.isEnabled = true
                autoButton.isEnabled = true//

                //для обновления игрового поля в фоновом потоке
                worker = object : SwingWorker<Array<Array<Int?>>, Any>() {
                    override fun doInBackground(): Array<Array<Int?>> {
                        while (!isCancelled) {
                            if (paused.get()) {
                                Thread.sleep(100)
                                continue
                            } else {
                                //gameView.stepGeneration(birthRule, survivalRule)
                                val newGameField = nextGeneration(gameField, survivalRule, birthRule)
                                gameField = newGameField
                                gameView.setGameField(gameField)
                                gameView.updateInfoGeneration()

                                gameView.repaint()
                                val repeated = saveAndCheckForRepetition(gameField, saveFolderPath)
                                if (repeated) {
                                    stopButton.doClick()
                                    break
                                }
                                publish()
                                Thread.sleep(2000)
                            }
                        }
                        return gameField
                    }

                    override fun process(chunks: List<Any>) {
                        gameView.repaint()
                    }

                    override fun done() {
                        startButton.isEnabled = true//
                        pauseButton.isEnabled = false
                        stopButton.isEnabled = false
                        generateButton.isEnabled = true
                        sizeField.isEnabled = true
                        fillCheckBox.isEnabled = true
                        autoButton.isEnabled = true
                        stepButton.isEnabled = true
                    }
                }

                worker?.execute()
            }
        })


        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                when (showConfirmationDialog()) {
                    0 -> {
                        val option = fileChooser.showSaveDialog(frame)
                        if (option == JFileChooser.APPROVE_OPTION) {
                            val selectedFile = fileChooser.selectedFile
                            saveStateToFile(gameField, selectedFile.absolutePath)
                            exitProcess(0)

                        }
                    }

                    1 -> {
                        exitProcess(0)
                    }

                    2 -> {
                    }
                }
            }

            override fun windowOpened(e: WindowEvent) {
                val openDialogResult = fileChooser.showOpenDialog(frame)
                if (openDialogResult == JFileChooser.APPROVE_OPTION) {
                    val openFile = fileChooser.selectedFile
                    val loadedField = loadStateFromFile(openFile)
                    gameField = loadedField
                    gameView.setGameField(loadedField)
                    gameView.isFocusable = true
                    gameView.setEditable(true)
                    gameView.repaint()
                    startButton.isEnabled = true
                    println("Состояние поля загружено из файла: ${openFile.absolutePath}")
                }
            }
        })


        val optionsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            add(JLabel("Field Size:"))
            add(JLabel("Rows:"))
            add(rowsField)
            add(Box.createRigidArea(Dimension(0, 10)))
            add(JLabel("Columns:"))
            add(colsField)
            add(Box.createRigidArea(Dimension(0, 10)))
            add(JLabel("Survival Rule:"))
            add(survivalField)
            add(Box.createRigidArea(Dimension(0, 10)))
            add(JLabel("Birth Rule:"))
            add(birthField)
            add(JLabel("Turn Count: "))
            add(turnCountField)
            add(autoButton)
            add(fillCheckBox)
        }

        stopButton.addActionListener {
            worker?.cancel(true)
            generateButton.isEnabled = true
            sizeField.isEnabled = true
            fillCheckBox.isEnabled = true
            startButton.isEnabled = false
            stopButton.isEnabled = false
            stepButton.isEnabled = false
            autoButton.isEnabled = true
        }

        pauseButton.addActionListener {
            autoButton.isEnabled = true
            paused.set(!paused.get())
            if (paused.get()) {
                pauseButton.text = "Resume"
            } else {
                pauseButton.text = "Pause"
            }
        }
        stepButton.addActionListener {
            val newGameField = gameField.let { nextGeneration(it, survivalRule, birthRule) }
            gameField = newGameField
            gameField.let { gameView.setGameField(it) }
            gameView.updateInfoGeneration()
            val repeated = saveAndCheckForRepetition(gameField, saveFolderPath)
            if (repeated) {
                stopButton.doClick()
            }
            stepButton.isEnabled = true
            gameView.repaint()
        }
        autoButton.addActionListener {
            val turnCount = turnCountField.text.toIntOrNull() ?: 10 // Получаем количество ходов из текстового поля
            generateButton.isEnabled = false
            startButton.isEnabled = false
            //stopButton.isEnabled = true
            pauseButton.isEnabled = true
            stepButton.isEnabled = false
            autoButton.isEnabled = false
            worker = object : SwingWorker<Array<Array<Int?>>, Any>() {
                override fun doInBackground(): Array<Array<Int?>> {
                    while (turnCount >= 0) {
                        if (!isCancelled) {
                            val newGameField = gameField.let { nextGeneration(it, survivalRule, birthRule) }
                            gameField = newGameField
                            gameField.let { gameView.setGameField(it) }
                            gameView.updateInfoGeneration()
                            val repeated = saveAndCheckForRepetition(gameField, saveFolderPath)
                            if (repeated) {
                                stopButton.doClick()
                                break
                            }
                            gameView.repaint()
                            Thread.sleep(500)
                        }
                    }
                    return gameField
                }

                override fun process(chunks: List<Any>) {
                    gameView.repaint()
                }

                override fun done() {
                    generateButton.isEnabled = true
                    startButton.isEnabled = true
                    //stopButton.isEnabled = false
                    pauseButton.isEnabled = true
                    stepButton.isEnabled = true
                    autoButton.isEnabled = true
                }
            }

            worker?.execute()
        }
        saveButton.addActionListener {
            val fileChooser = JFileChooser()
            val filter = FileNameExtensionFilter("Txt Files", "txt")
            fileChooser.fileFilter = filter
            val returnValue = fileChooser.showSaveDialog(frame)
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                val field = gameView.getGameField()
                if (field != null) {
                    //field.saveStateToFile(file.absolutePath)
                    saveStateToFile(gameField, file.absolutePath)
                    JOptionPane.showMessageDialog(null, "Состояние игры успешно сохранено!")

                }
            }
        }


        loadButton.addActionListener {
            val fileChooser = JFileChooser()
            val filter = FileNameExtensionFilter("Text Files", "txt")
            fileChooser.fileFilter = filter
            val returnValue = fileChooser.showOpenDialog(frame)
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                gameField = loadStateFromFile(file)
                gameView.setGameField(gameField)
                gameView.isFocusable = true
                gameView.clearInfoGeneration()
                gameView.setEditable(true)
                gameView.repaint()
            }
            generateButton.isEnabled = true
            startButton.isEnabled = true
        }

        val buttonsPanel = JPanel().apply {
            add(generateButton)
            add(startButton)
            add(stopButton)
            add(pauseButton)
            add(stepButton)
            add(saveButton)
            add(loadButton)
        }

        frame.layout = BorderLayout()
        frame.add(gameView, BorderLayout.CENTER)
        frame.preferredSize = Dimension(screenWidth, screenHeight)
        frame.add(buttonsPanel, BorderLayout.SOUTH)
        frame.add(optionsPanel, BorderLayout.EAST)
        frame.pack()

        frame.isVisible = true

    }
}



