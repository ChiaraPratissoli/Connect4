package com.example.connect4;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.Objects;


public class Connect4Controller  {
    @FXML
    private GridPane gridPane;

    @FXML
    private Label turnLabel;

    private final int ROWS = 6;
    private final int COLUMNS = 7;
    private final Circle[][] cells = new Circle[ROWS][COLUMNS];
    private Connect4LogicGame logicGame = new Connect4LogicGame();

    @FXML
    public void initialize(){
        drawBoard();
    }

    @FXML
    void resetGame(MouseEvent event) {
        logicGame = new Connect4LogicGame();
        drawBoard();
    }

    private void drawBoard(){
        gridPane.getChildren().clear();

        turnLabel.setText("Current turn: Player 1 (Red)");
        turnLabel.setTextFill(Color.RED);

        double minRadius = 15;

        for (int row = 0; row < ROWS; row++){
            for (int column = 0; column < COLUMNS; column++){
                Circle circle = new Circle();
                circle.radiusProperty().bind(createRadiusBinding(gridPane, 0.9, 15));
                circle.setFill(Color.WHITE);
                circle.setStroke(Color.BLACK);
                cells[row][column] = circle;
                gridPane.add(circle, column, row);

                GridPane.setHalignment(circle, HPos.CENTER);
                GridPane.setValignment(circle, VPos.CENTER);

                final int currentCol = column;
                circle.setOnMouseClicked(e -> playMove(currentCol));
            }
        }
    }

    public void playMove(int column){
        Connect4LogicGame.Move move = logicGame.insertDisc(column);
        if (move != null){
            Color color = (move.player == 1) ? Color.RED : Color.YELLOW;

            Circle disc = new Circle();
            disc.setFill(color);
            disc.radiusProperty().bind(createRadiusBinding(gridPane, 0.9, 15));

            // Aggiungo il disco nella posizione di partenza (sopra la colonna scelta, riga 0)
            gridPane.add(disc, column, 0);
            GridPane.setHalignment(disc, HPos.CENTER);
            GridPane.setValignment(disc, VPos.CENTER);

            // Calcolo spostamento verticale in pixel
            double cellHeight = gridPane.getHeight() / ROWS;
            double toY = move.row * cellHeight;

            TranslateTransition tt = new TranslateTransition(
                    Duration.millis(120 * (move.row + 1)),
                    disc
            );
            tt.setFromY(0);
            tt.setToY(toY);
            tt.setInterpolator(Interpolator.LINEAR);
            tt.setOnFinished(e -> {
                gridPane.getChildren().remove(disc);
                cells[move.row][column].setFill(color);

                updateTurnLabel(logicGame.isPlayerOneTurn());

            });
            tt.play();

            playSound("/sounds/coin.mp3");

            if (logicGame.checkWin(move.player))
                showWinner(move.player);
            else if (logicGame.isBoardFull())
                showDraw();
        }
    }

    private DoubleBinding createRadiusBinding(GridPane gridPane, double marginFactor, double minRadius){
        return Bindings.createDoubleBinding(
                () -> {
                    double cellHeight = gridPane.getHeight() / ROWS;
                    double cellWidth = gridPane.getWidth() / COLUMNS;
                    return Math.max(minRadius, Math.min(cellHeight, cellWidth) / 2 * marginFactor);
                },
                gridPane.widthProperty(),
                gridPane.heightProperty()
        );
    }

    public void playSound(String path) {
        String uri = Objects.requireNonNull(getClass().getResource(path)).toExternalForm();
        AudioClip audioClip = new AudioClip(uri);
        audioClip.play();
    }

    private void updateTurnLabel(boolean isPlayerOneTurn) {
        if (isPlayerOneTurn) {
            turnLabel.setText("Current turn: Player 1 (Red)");
            turnLabel.setTextFill(Color.RED);
        } else {
            turnLabel.setText("Current turn: Player 2 (Yellow)");
            turnLabel.setTextFill(Color.YELLOW);
        }
    }

    private void showDraw() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "It's a draw!", ButtonType.OK);
        alert.setHeaderText("Game over");
        alert.setTitle("Connect 4");
        alert.showAndWait();
        resetGame(null);
    }

    private void showWinner(int player){
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Player " + player + " won the game!", ButtonType.OK);
        alert.setHeaderText("Game over");
        alert.setTitle("Connect 4");
        alert.showAndWait();

        resetGame(null);
    }


}
