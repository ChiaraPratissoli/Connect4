package com.example.connect4;

public class Connect4LogicGame {

    public class Move {
        public final int row;
        public final int column;
        public final int player;

        public Move(int row, int column, int player) {
            this.row = row;
            this.column = column;
            this.player = player;
        }
    }

    private final int ROWS = 6;
    private final int COLUMNS = 7;
    private final int[][] board = new int[ROWS][COLUMNS];
    private boolean playerOneTurn = true;

    public Move insertDisc(int column){
        for (int row = ROWS - 1; row >= 0; row--){
            if (board[row][column] == 0){
                int player = playerOneTurn ? 1 : 2;
                board[row][column] = player;
                playerOneTurn = !playerOneTurn;
                return new Move(row, column, player);
            }
        }
        return null;  // colonna piena
    }

    public boolean isPlayerOneTurn(){
        return playerOneTurn;
    }

    public boolean checkWin(int player){
        // Orizzontale
        for (int row = 0; row < ROWS; row++){
            for (int column = 0; column < 4; column++){
                if (board[row][column] == player && board[row][column + 1] == player &&
                        board[row][column + 2] == player && board[row][column + 3] == player){
                    return true;
                }
            }
        }

        // Verticale
        for (int column = 0; column < COLUMNS; column++){
            for (int row = 0; row < 3; row++){
                if (board[row][column] == player && board[row + 1][column] == player &&
                        board[row + 2][column] == player && board[row + 3][column] == player){
                    return true;
                }
            }
        }

        // Diagonale
        for (int row = 0; row < 3; row++){
            for (int column = 0; column < 4; column++){
                if (board[row][column] == player && board[row + 1][column + 1] == player &&
                        board[row + 2][column + 2] == player && board[row + 3][column + 3] == player){
                    return true;
                }
            }
        }

        // Diagonale
        for (int row = 0; row < 3; row++){
            for (int column = COLUMNS - 1; column >= 3; column--){
                if (board[row][column] == player && board[row + 1][column - 1] == player &&
                        board[row + 2][column - 2] == player && board[row + 3][column - 3] == player) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isBoardFull() {
        for (int column = 0; column < COLUMNS; column++) {
            if (board[0][column] == 0)
                return false;
        }
        return true;
    }
}
