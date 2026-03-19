package org.geantlr;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    private boolean isDarkMode = true;

    @Override
    public void start(Stage stage) throws IOException {
        // Initial Theme
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/org/geantlr/views/MainView.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 800, 600);

        // Load custom theme overrides
        String customCss = getClass().getResource("/org/geantlr/theme.css").toExternalForm();
        scene.getStylesheets().add(customCss);

        stage.setTitle("GeantLR Editor Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
