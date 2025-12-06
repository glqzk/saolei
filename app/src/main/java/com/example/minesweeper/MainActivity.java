package com.example.minesweeper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int ROWS = 10;
    private static final int COLS = 10;
    private static final int MINES = 10;

    private Button[][] buttons;
    private boolean[][] mines;
    private boolean[][] uncovered;
    private boolean[][] flagged;
    private int[][] counts;
    private int remainingMines;
    private boolean gameOver;

    private GridLayout gameBoard;
    private TextView mineCountText;
    private Button restartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameBoard = findViewById(R.id.game_board);
        mineCountText = findViewById(R.id.mine_count);
        restartButton = findViewById(R.id.restart_button);

        restartButton.setOnClickListener(v -> restartGame());

        initializeGame();
    }

    private void initializeGame() {
        buttons = new Button[ROWS][COLS];
        mines = new boolean[ROWS][COLS];
        uncovered = new boolean[ROWS][COLS];
        flagged = new boolean[ROWS][COLS];
        counts = new int[ROWS][COLS];
        remainingMines = MINES;
        gameOver = false;

        gameBoard.removeAllViews();
        generateMines();
        calculateCounts();
        createButtons();
        updateMineCount();
    }

    private void generateMines() {
        int minesPlaced = 0;
        while (minesPlaced < MINES) {
            int row = (int) (Math.random() * ROWS);
            int col = (int) (Math.random() * COLS);
            if (!mines[row][col]) {
                mines[row][col] = true;
                minesPlaced++;
            }
        }
    }

    private void calculateCounts() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (!mines[row][col]) {
                    counts[row][col] = countAdjacentMines(row, col);
                }
            }
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = row + i;
                int newCol = col + j;
                if (isValidCell(newRow, newCol) && mines[newRow][newCol]) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    private void createButtons() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Button button = new Button(this);
                button.setTag(new int[]{row, col});
                button.setBackgroundColor(getResources().getColor(R.color.button_default));
                button.setTextSize(18);
                button.setElevation(2);

                button.setOnClickListener(v -> onButtonClick((Button) v));
                button.setOnLongClickListener(v -> onButtonLongClick((Button) v));

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(row, 1f);
                params.columnSpec = GridLayout.spec(col, 1f);
                params.setMargins(1, 1, 1, 1);

                button.setLayoutParams(params);
                buttons[row][col] = button;
                gameBoard.addView(button);
            }
        }
    }

    private void onButtonClick(Button button) {
        if (gameOver) return;

        int[] tag = (int[]) button.getTag();
        int row = tag[0];
        int col = tag[1];

        if (uncovered[row][col] || flagged[row][col]) return;

        uncovered[row][col] = true;

        if (mines[row][col]) {
            gameOver = true;
            revealMines();
            showGameOverDialog(false);
        } else {
            updateButtonAppearance(row, col);
            if (counts[row][col] == 0) {
                uncoverAdjacentCells(row, col);
            }
            checkWinCondition();
        }
    }

    private boolean onButtonLongClick(Button button) {
        if (gameOver) return false;

        int[] tag = (int[]) button.getTag();
        int row = tag[0];
        int col = tag[1];

        if (uncovered[row][col]) return false;

        flagged[row][col] = !flagged[row][col];
        remainingMines += flagged[row][col] ? -1 : 1;
        updateMineCount();
        button.setText(flagged[row][col] ? "🚩" : "");

        return true;
    }

    private void updateButtonAppearance(int row, int col) {
        Button button = buttons[row][col];
        button.setBackgroundColor(getResources().getColor(R.color.uncovered));
        button.setElevation(0);

        if (counts[row][col] > 0) {
            button.setText(String.valueOf(counts[row][col]));
            setTextColor(button, counts[row][col]);
        }
    }

    private void setTextColor(Button button, int count) {
        switch (count) {
            case 1: button.setTextColor(getResources().getColor(android.R.color.holo_blue_dark)); break;
            case 2: button.setTextColor(getResources().getColor(android.R.color.holo_green_dark)); break;
            case 3: button.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); break;
            case 4: button.setTextColor(getResources().getColor(android.R.color.holo_purple)); break;
            case 5: button.setTextColor(getResources().getColor(android.R.color.holo_orange_dark)); break;
            case 6: button.setTextColor(getResources().getColor(android.R.color.holo_cyan_dark)); break;
            case 7: button.setTextColor(getResources().getColor(android.R.color.black)); break;
            case 8: button.setTextColor(getResources().getColor(android.R.color.darker_gray)); break;
        }
    }

    private void uncoverAdjacentCells(int row, int col) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = row + i;
                int newCol = col + j;
                if (isValidCell(newRow, newCol) && !uncovered[newRow][newCol] && !flagged[newRow][newCol]) {
                    uncovered[newRow][newCol] = true;
                    updateButtonAppearance(newRow, newCol);
                    if (counts[newRow][newCol] == 0) {
                        uncoverAdjacentCells(newRow, newCol);
                    }
                }
            }
        }
    }

    private void revealMines() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (mines[row][col]) {
                    buttons[row][col].setText("💣");
                    buttons[row][col].setBackgroundColor(getResources().getColor(R.color.mine_color));
                }
            }
        }
    }

    private void checkWinCondition() {
        int uncoveredCells = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (uncovered[row][col]) {
                    uncoveredCells++;
                }
            }
        }

        if (uncoveredCells == ROWS * COLS - MINES) {
            gameOver = true;
            showGameOverDialog(true);
        }
    }

    private void showGameOverDialog(boolean won) {
        String message = won ? getString(R.string.win_message) : getString(R.string.lose_message);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(won ? "胜利！" : "失败！")
                .setMessage(message)
                .setPositiveButton("确定", (dialog, which) -> dialog.dismiss())
                .setNegativeButton(getString(R.string.restart), (dialog, which) -> {
                    dialog.dismiss();
                    restartGame();
                })
                .show();
    }

    private void restartGame() {
        new AlertDialog.Builder(this)
                .setTitle("重新开始")
                .setMessage(getString(R.string.confirm_restart))
                .setPositiveButton("确定", (dialog, which) -> {
                    dialog.dismiss();
                    initializeGame();
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateMineCount() {
        mineCountText.setText("地雷: " + remainingMines);
    }
}