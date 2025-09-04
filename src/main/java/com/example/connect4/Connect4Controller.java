package com.example.connect4;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.Objects;
import java.util.Optional;


public class Connect4Controller  {
    @FXML
    private GridPane gridPane;
    @FXML
    private Label turnLabel;
    @FXML
    private ToggleButton muteButton;

    private static final int ROWS = 6;
    private static final int COLUMNS = 7;
    private static final Circle[][] cells = new Circle[ROWS][COLUMNS];
    private Connect4LogicGame logicGame = new Connect4LogicGame();

    private static final double marginFactor = 0.9;
    private static final double minRadius = 15;

    private AudioClip coinSound;
    private boolean soundOn = true;

    @FXML
    public void initialize(){
        showWelcomePopup();
        drawBoard();
        coinSound = new AudioClip(Objects.requireNonNull(getClass().getResource("/sounds/coin.mp3")).toExternalForm());
    }

    @FXML
    void resetGame() {
        logicGame = new Connect4LogicGame();
        drawBoard();
    }

    @FXML
    void sound() {
        soundOn = !soundOn;
        muteButton.setText(soundOn ? "🔊 Sound On" : "🔇 Muted");
    }

    private void showWelcomePopup(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Welcome to Connect 4!");
        alert.setHeaderText("Rules of the Game");
        alert.setContentText(
                """
                        1) Players take turns dropping colored discs into the grid.
                        2) The first player to connect four discs in a row wins.
                           (Horizontal, Vertical, or Diagonal)
                        3) If the grid fills up with no winner, the game ends in a draw.
                           Good luck and have fun!""");

        alert.getButtonTypes().setAll(ButtonType.OK);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/popup-style.css")).toExternalForm());

        ImageView image = new ImageView(new Image(Objects.requireNonNull(
                Objects.requireNonNull(getClass().getResource("/icons/welcome_icon.png")).toExternalForm())));

        image.setFitWidth(64);
        image.setFitHeight(64);
        alert.setGraphic(image);

        alert.showAndWait();
    }

    private void drawBoard(){
        gridPane.getChildren().clear();

        turnLabel.setText("Current turn: Player 1 (Red)");
        turnLabel.setTextFill(Color.RED);

        for (int row = 0; row < ROWS; row++){
            for (int column = 0; column < COLUMNS; column++){
                Circle circle = new Circle();
                circle.radiusProperty().bind(createRadiusBinding(gridPane));
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
        addColumnHoverEffect();
    }

    private void playMove(int column){
        Move move = logicGame.insertDisc(column);
        if (move != null){
            Color color = (move.player() == 1) ? Color.RED : Color.YELLOW;

            Circle disc = new Circle();
            disc.setFill(color);
            disc.radiusProperty().bind(createRadiusBinding(gridPane));

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

            playCoinSound();

            if (logicGame.checkWin(move.player()))
                showEndGame("Player " + move.player() + " wins!",
                        "Congratulations! Player " + move.player() + " has won the game.",
                        "/icons/winner_icon.png");
            else if (logicGame.isBoardFull())
                showEndGame("⚖ Draw!", "The board is full and no one has won.",
                        "/icons/draw_icon.png");
        }
    }

    private DoubleBinding createRadiusBinding(GridPane gridPane){
        return Bindings.createDoubleBinding(
                () -> {
                    double cellHeight = gridPane.getHeight() / ROWS;
                    double cellWidth = gridPane.getWidth() / COLUMNS;
                    return Math.max(Connect4Controller.minRadius, Math.min(cellHeight, cellWidth) / 2 * Connect4Controller.marginFactor);
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

    private void playCoinSound(){
        if (soundOn && coinSound != null)
            coinSound.play();
    }

    private void addColumnHoverEffect(){
        for (int column = 0; column < COLUMNS; column++){
            final int currentColumn = column;
            for (int row = 0; row < ROWS; row++){
                Circle circle = cells[row][column];

                circle.setOnMouseEntered(e -> highlightColumn(currentColumn, true));
                circle.setOnMouseExited(e -> highlightColumn(currentColumn, false));
            }
        }
    }

    private void highlightColumn(int column, boolean highlight){
        for (int row = 0; row < ROWS; row++){
            Circle circle = cells[row][column];
            if (highlight){
                circle.setStroke(logicGame.isPlayerOneTurn() ? Color.RED : Color.YELLOW);
                circle.setStrokeWidth(3);
            }
            else {
                circle.setStroke(Color.BLACK);
                circle.setStrokeWidth(1);
            }
        }
    }

    private void showEndGame(String header, String content, String iconPath){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connect 4");
        alert.setHeaderText(header);
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/popup-style.css")).toExternalForm());

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
            resetGame();
        else if (result.isPresent() && result.get() == exit)
            Platform.exit();
    }
}
