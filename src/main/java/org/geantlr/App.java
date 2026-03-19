package org.geantlr;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

public class App extends Application {

    private boolean isDarkMode = true;

    @Override
    public void start(Stage stage) {
        // Initial Theme
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        // Ensure background uses standard base theme color or custom variables
        root.getStyleClass().add("background");

        Label title = new Label("AtlantaFX & Ikonli Theming");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // FontIcon setup
        FontIcon toggleIcon = new FontIcon("mdi2w-white-balance-sunny");
        toggleIcon.setIconSize(24);

        Button toggleBtn = new Button("Toggle Light/Dark Mode", toggleIcon);
        // Use an accent button style if provided by AtlantaFX
        toggleBtn.getStyleClass().addAll("accent");

        toggleBtn.setOnAction(e -> {
            isDarkMode = !isDarkMode;
            if (isDarkMode) {
                Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
                toggleIcon.setIconLiteral("mdi2w-white-balance-sunny");
            } else {
                Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
                toggleIcon.setIconLiteral("mdi2m-moon-waning-crescent");
            }
        });

        root.getChildren().addAll(title, toggleBtn);

        Scene scene = new Scene(root, 640, 480);

        // Load custom theme overrides
        String customCss = getClass().getResource("/org/geantlr/theme.css").toExternalForm();
        scene.getStylesheets().add(customCss);

        stage.setTitle("GeantLR Theming Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
