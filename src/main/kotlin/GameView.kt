import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import javax.swing.JButton
import javax.swing.JPanel
import kotlin.math.max
import kotlin.math.min

class GameView(private var width: Int, private var height: Int) : JPanel() {

    private var isEditable = false
    private var generationCounts: Array<IntArray>? = null
    private var mouseX: Int = -1
    private var mouseY: Int = -1
    private var cellSize = 10

    private var startCol = 0
    private var startRow = 0

    private var offsetX = 0
    private var offsetY = 0
    private val scrollUpButton = JButton("▲")
    private val scrollDownButton = JButton("▼")
    private val scrollLeftButton = JButton("◀")
    private val scrollRightButton = JButton("▶")


    // Поле
    private var gameField: Array<Array<Int?>>? = null

    init {
        isFocusable = true
        requestFocusInWindow()


        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (isEditable && gameField != null) {
                    startRow = max(0, (mouseY - offsetY * cellSize) / cellSize)
                    startCol = max(0, (mouseX - offsetX * cellSize) / cellSize)
                    val col = e.x / cellSize + startCol
                    val row = e.y / cellSize + startRow
                    if (row in gameField!!.indices && col in gameField!![row].indices) {
                        val cell = gameField!![row + offsetY][col + offsetX]
                        gameField!![row + offsetY][col + offsetX] = when (cell) {
                            2 -> null
                            null -> 0
                            else -> cell + 1
                        }
                        repaint()
                    }
                }
            }
        })


        addMouseWheelListener(object : MouseWheelListener {
            override fun mouseWheelMoved(e: MouseWheelEvent) {
                startCol = max(0, mouseX / cellSize)
                startRow = max(0, mouseY / cellSize)
                val rotation = e.wheelRotation
                cellSize = max(1, cellSize - rotation)
                // Обновляем положение курсора мыши с учетом изменения масштаба
                val mouseXCell = (mouseX / cellSize) * cellSize
                val mouseYCell = (mouseY / cellSize) * cellSize
                mouseX = mouseXCell
                mouseY = mouseYCell
                repaint()
            }
        })
        scrollUpButton.addActionListener {
            offsetY = max(0, offsetY - 1)
            mouseX -= offsetX * cellSize // Обновляем mouseX после смещения поля
            mouseY -= offsetY * cellSize // Обновляем mouseY после смещения поля
            repaint()
        }

        scrollDownButton.addActionListener {
            offsetY = min(gameField!![0].size - height / cellSize, offsetY + 1)
            mouseX -= offsetX * cellSize // Обновляем mouseX после смещения поля
            mouseY -= offsetY * cellSize // Обновляем mouseY после смещения поля
            repaint()
        }

        scrollLeftButton.addActionListener {
            offsetX = max(0, offsetX - 1)
            mouseX -= offsetX * cellSize // Обновляем mouseX после смещения поля
            mouseY -= offsetY * cellSize // Обновляем mouseY после смещения поля
            repaint()
        }

        scrollRightButton.addActionListener {
            offsetX = min(gameField!!.size - width / cellSize, offsetX + 1)
            mouseX -= offsetX * cellSize // Обновляем mouseX после смещения поля
            mouseY -= offsetY * cellSize // Обновляем mouseY после смещения поля
            repaint()
        }


        val panel = JPanel()
        panel.add(scrollLeftButton)
        panel.add(scrollUpButton)
        panel.add(scrollDownButton)
        panel.add(scrollRightButton)
        add(panel, BorderLayout.SOUTH)


    }


    fun setGameField(gameField: Array<Array<Int?>>) {
        this.gameField = gameField
        clearInfoGeneration()
    }

    fun getGameField(): Array<Array<Int?>>? {
        return gameField
    }

    fun updateInfoGeneration() {
        // Обновление информации о поколениях
        generationCounts = Array(gameField!!.size) { row ->
            IntArray(gameField!![row].size) { col ->
                if (gameField!![row][col] != null) {
                    generationCounts?.get(row)?.get(col)?.plus(1) ?: 0
                } else {
                    0
                }
            }
        }
    }

    fun clearInfoGeneration() {
        /*generationCounts = Array(gameField!!.size) { row ->
            IntArray(gameField!![row].size) { col ->
                0
            }
        }*/
        generationCounts = null
        updateInfoGeneration()
    }


    override fun setSize(newRows: Int, newCols: Int) {
        this.width = newRows
        this.height = newCols
        preferredSize = Dimension(width, height)
        revalidate()
    }

    fun setEditable(editable: Boolean) {
        this.isEditable = editable
    }


    @Synchronized
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (gameField == null) {
            return
        }

        val g2d = g.create() as Graphics2D
        g2d.color = background
        g2d.fillRect(0, 0, width, height)

        val rows = gameField!!.size
        val cols = gameField!![0].size

        val visibleRows = height / cellSize
        val visibleCols = width / cellSize

        val startRow = max(0, offsetY)
        val endRow = min(rows, offsetY + visibleRows)

        val startCol = max(0, offsetX)
        val endCol = min(cols, offsetX + visibleCols)

        val mouseXCell = (mouseX + offsetX * cellSize) / cellSize
        val mouseYCell = (mouseY + offsetY * cellSize) / cellSize
        mouseX = mouseXCell
        mouseY = mouseYCell

        for (row in startRow until endRow) {
            for (col in startCol until endCol) {
                if (row < rows && col < cols) {
                    val value = gameField!![row][col]
                    val isHovered = mouseX == col && mouseY == row

                    val x = (col - offsetX) * cellSize
                    val y = (row - offsetY) * cellSize
                    g2d.color = Color.GRAY
                    g2d.drawRect(x, y, cellSize, cellSize)
                    if (value != null) {
                        g2d.color = getColor(value)
                        g2d.fillRect(x, y, cellSize, cellSize)
                        if (isHovered) {
                            // Отображение информации о поколении - не работает пока
                            val generationCount = generationCounts?.get(row)?.get(col)
                            if (generationCount != null) {
                                val text = generationCount.toString()
                                val fontMetrics = g2d.fontMetrics
                                val textWidth = fontMetrics.stringWidth(text)
                                val textHeight = fontMetrics.height
                                val textX = x + (cellSize - textWidth) / 2
                                val textY = y + (cellSize + textHeight) / 2
                                g2d.color = Color.GREEN
                                val fontSize = (cellSize * 0.5).toInt()
                                g2d.font = Font(g2d.font.fontName, Font.PLAIN, fontSize)
                                g2d.drawString(text, textX, textY)
                            }
                        }
                    }
                }
            }
        }
        g2d.dispose()
    }


    private fun getColor(colorIndex: Int?): Color {
        return when (colorIndex) {
            0 -> Color.RED
            1 -> Color.BLACK
            2 -> Color.BLUE
            else -> Color.WHITE
        }
    }
}