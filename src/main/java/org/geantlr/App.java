package org.geantlr;

import atlantafx.base.theme.PrimerDark;
import io.micronaut.context.ApplicationContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class App extends Application {

    private ApplicationContext context;

    @Override
    public void init() {
        context = ApplicationContext.run();
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Initial Theme
        setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/org/geantlr/views/MainView.fxml"));
        fxmlLoader.setControllerFactory(context.getBean(FxmlControllerFactory.class));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 1200, 600);

        // Load custom theme overrides
        String customCss = getClass().getResource("/org/geantlr/theme.css").toExternalForm();
        scene.getStylesheets().add(customCss);

        stage.setTitle("geANTLR Editor Demo");
        stage.initStyle(StageStyle.EXTENDED);
        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {
        System.setProperty("javafx.enablePreview", "true");
        launch();
    }
}
