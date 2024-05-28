package toc.group9.assignment;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Scene scene;

    @SuppressWarnings("exports")
    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(new FXMLLoader(Main.class.getResource("main.fxml")).load(), 800, 600);
        stage.setTitle("CSE6344 TT2L Group 9 Assignment");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}