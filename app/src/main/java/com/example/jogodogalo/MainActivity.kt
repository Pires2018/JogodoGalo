package com.example.jogodogalo

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var buttons: Array<Array<Button>>
    private var player = true // true for X, false for O
    private var boardStatus = Array(3) { IntArray(3) } // 0 for empty, 1 for X, 2 for O
    private var difficulty = "Fácil"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arrayOf(
            arrayOf(
                findViewById(R.id.button1) ,
                findViewById(R.id.button2) ,
                findViewById(R.id.button3)
            ) ,
            arrayOf(
                findViewById(R.id.button4) ,
                findViewById(R.id.button5) ,
                findViewById(R.id.button6)
            ) ,
            arrayOf(
                findViewById(R.id.button7) ,
                findViewById(R.id.button8) ,
                findViewById(R.id.button9)
            )
        ).also { buttons = it }

        val difficultySpinner: Spinner = findViewById(R.id.difficultySpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.difficulty_levels,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            difficultySpinner.adapter = adapter
        }

        difficultySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                difficulty = parent.getItemAtPosition(position).toString()
                resetBoard()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        for (i in buttons.indices) {
            for (j in buttons[i].indices) {
                buttons[i][j].setOnClickListener {
                    onButtonClick(i, j)
                }
            }
        }

        initializeBoardStatus()
    }

    private fun initializeBoardStatus() {
        for (i in 0..2) {
            for (j in 0..2) {
                boardStatus[i][j] = 0
                buttons[i][j].text = ""
                buttons[i][j].isEnabled = true
            }
        }
    }

    private fun onButtonClick(row: Int, col: Int) {
        if (boardStatus[row][col] == 0) {
            boardStatus[row][col] = if (player) 1 else 2
            buttons[row][col].text = if (player) "X" else "O"
            buttons[row][col].isEnabled = false

            if (checkWinner()) {
                val winner = if (player) "X" else "O"
                Toast.makeText(this, "$winner Wins!", Toast.LENGTH_SHORT).show()
                resetBoard()
            } else if (isBoardFull()) {
                Toast.makeText(this, "It's a Draw!", Toast.LENGTH_SHORT).show()
                resetBoard()
            } else {
                player = !player
                if (!player) {
                    computerMove()
                }
            }
        }
    }

    private fun computerMove() {
        if (difficulty == "Fácil") {
            easyComputerMove()
        } else {
            hardComputerMove()
        }

        if (checkWinner()) {
            val winner = if (player) "X" else "O"
            Toast.makeText(this, "$winner Wins!", Toast.LENGTH_SHORT).show()
            resetBoard()
        } else if (isBoardFull()) {
            Toast.makeText(this, "It's a Draw!", Toast.LENGTH_SHORT).show()
            resetBoard()
        } else {
            player = !player
        }
    }

    private fun easyComputerMove() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) {
            for (j in 0..2) {
                if (boardStatus[i][j] == 0) {
                    emptyCells.add(Pair(i, j))
                }
            }
        }
        if (emptyCells.isNotEmpty()) {
            val (row, col) = emptyCells[Random.nextInt(emptyCells.size)]
            boardStatus[row][col] = 2
            buttons[row][col].text = "O"
            buttons[row][col].isEnabled = false
        }
    }

    private fun hardComputerMove() {
        var bestScore = Int.MIN_VALUE
        var bestMove: Pair<Int, Int>? = null

        for (i in 0..2) {
            for (j in 0..2) {
                if (boardStatus[i][j] == 0) {
                    boardStatus[i][j] = 2
                    val score = minimax(boardStatus, false)
                    boardStatus[i][j] = 0
                    if (score > bestScore) {
                        bestScore = score
                        bestMove = Pair(i, j)
                    }
                }
            }
        }

        if (bestMove != null) {
            val (row, col) = bestMove
            boardStatus[row][col] = 2
            buttons[row][col].text = "O"
            buttons[row][col].isEnabled = false
        }
    }

    private fun minimax(board: Array<IntArray>, isMaximizing: Boolean): Int {
        if (checkWinner()) {
            return if (player) -1 else 1
        }
        if (isBoardFull()) {
            return 0
        }

        return if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j] == 0) {
                        board[i][j] = 2
                        val score = minimax(board, false)
                        board[i][j] = 0
                        bestScore = maxOf(score, bestScore)
                    }
                }
            }
            bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j] == 0) {
                        board[i][j] = 1
                        val score = minimax(board, true)
                        board[i][j] = 0
                        bestScore = minOf(score, bestScore)
                    }
                }
            }
            bestScore
        }
    }

    private fun checkWinner(): Boolean {
        // Verifica linhas
        for (i in 0..2) {
            if (boardStatus[i][0] == boardStatus[i][1] && boardStatus[i][1] == boardStatus[i][2] && boardStatus[i][0] != 0) {
                return true
            }
        }
        // Verifica colunas
        for (i in 0..2) {
            if (boardStatus[0][i] == boardStatus[1][i] && boardStatus[1][i] == boardStatus[2][i] && boardStatus[0][i] != 0) {
                return true
            }
        }
        // Verifica diagonais
        if (boardStatus[0][0] == boardStatus[1][1] && boardStatus[1][1] == boardStatus[2][2] && boardStatus[0][0] != 0) {
            return true
        }
        if (boardStatus[0][2] == boardStatus[1][1] && boardStatus[1][1] == boardStatus[2][0] && boardStatus[0][2] != 0) {
            return true
        }
        return false
    }

    private fun isBoardFull(): Boolean {
        for (i in 0..2) {
            for (j in 0..2) {
                if (boardStatus[i][j] == 0) {
                    return false
                }
            }
        }
        return true
    }

    private fun resetBoard() {
        player = true
        initializeBoardStatus()
    }
}