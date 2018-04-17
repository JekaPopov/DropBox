package ru.dravn.dropbox.Client;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class ClientStart extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("drop_box_client.fxml"));
        primaryStage.setTitle("Чат");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }

}