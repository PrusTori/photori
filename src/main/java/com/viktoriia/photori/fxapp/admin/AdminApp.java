package com.viktoriia.photori.fxapp.admin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdminApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/admin.fxml"));
        primaryStage.setTitle("Окно для администратора");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

}
