package com.example.connect4;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class Connect4Application extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
       FXMLLoader loader = new FXMLLoader(getClass().getResource("connect4.fxml"));
       Scene scene = new Scene(loader.load());
       stage.setTitle("Connect 4");
       stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/connect4_icon.png"))));
       stage.setScene(scene);
       stage.show();
    }
}
