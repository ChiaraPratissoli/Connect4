package com.example.connect4;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;


public class Connect4Controller  {
    @FXML
    private GridPane gridPane;

    @FXML
    private Label turnLabel;

    private final int ROWS = 6;
    private final int COLUMNS = 7;
    private final Circle[][] cells = new Circle[ROWS][COLUMNS];
    private Connect4LogicGame logicGame = new Connect4LogicGame();

    private AudioClip coinSound;

    @FXML
    public void initialize(){
        drawBoard();
        coinSound = new AudioClip(Objects.requireNonNull(getClass().getResource("/sounds/coin.mp3")).toExternalForm());
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

    private void playMove(int column){
        Move move = logicGame.insertDisc(column);
        if (move != null){
            Color color = (move.player() == 1) ? Color.RED : Color.YELLOW;

            Circle disc = new Circle();
            disc.setFill(color);
            disc.radiusProperty().bind(createRadiusBinding(gridPane, 0.9, 15));

            // Aggiungo il disco nella posizione di partenza (sopra la colonna scelta, riga 0)
            gridPane.add(disc, column, 0);
            GridPane.setHalignment(disc, HPos.CENTER);
            GridPane.setValignment(disc, VPos.CENTER);

            // Calcolo spostamento verticale in pixel
            double cellHeight = gridPane.getHeight() / ROWS;
            double toY = move.row() * cellHeight;

            TranslateTransition tt = new TranslateTransition(
                    Duration.millis(120 * (move.row() + 1)),
                    disc
            );
            tt.setFromY(0);
            tt.setToY(toY);
            tt.setInterpolator(Interpolator.LINEAR);
            tt.setOnFinished(e -> {
                gridPane.getChildren().remove(disc);
                cells[move.row()][column].setFill(color);

                updateTurnLabel(logicGame.isPlayerOneTurn());

            });
            tt.play();

            coinSound.play();

            if (logicGame.checkWin(move.player()))
                showEndGame("Player " + move.player() + " wins!",
                        "Congratulations! Player " + move.player() + " has won the game.",
                        "/icons/winner_icon.png");
            else if (logicGame.isBoardFull())
                showEndGame("⚖ Draw!", "The board is full and no one has won.",
                        "/icons/draw_icon.png");
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


    private void updateTurnLabel(boolean isPlayerOneTurn) {
        if (isPlayerOneTurn) {
            turnLabel.setText("Current turn: Player 1 (Red)");
            turnLabel.setTextFill(Color.RED);
        } else {
            turnLabel.setText("Current turn: Player 2 (Yellow)");
            turnLabel.setTextFill(Color.YELLOW);
        }
    }

    private void showEndGame(String header, String content, String iconPath){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connect 4");
        alert.setHeaderText(header);
        alert.setContentText(content);

        ButtonType newGame = new ButtonType("New Game");
        ButtonType exit = new ButtonType("Exit");
        alert.getButtonTypes().setAll(newGame, exit);

        if(iconPath != null){
            ImageView image = new ImageView(new Image(
                    Objects.requireNonNull(Objects.requireNonNull(getClass().getResource(iconPath)).toExternalForm())
            ));

            image.setFitHeight(50);
            image.setFitWidth(50);
            alert.setGraphic(image);
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == newGame)
            resetGame(null);
        else if (result.isPresent() && result.get() == exit)
            Platform.exit();
    }
}
