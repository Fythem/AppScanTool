package com.fythem.AppScanTool;

import javafx.application.Application;
import javafx.stage.Stage;

public class StartScanApplication extends Application {

    public static void main(String[] strings) {
        System.out.println("start!");
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("ScanApp");
        primaryStage.show();
    }
}
