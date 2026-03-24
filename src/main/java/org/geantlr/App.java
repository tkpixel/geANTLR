package org.geantlr;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import io.micronaut.context.ApplicationContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class App extends Application {

    private boolean isDarkMode = true;
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
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/org/geantlr/views/MainView.fxml"));
        fxmlLoader.setControllerFactory(context.getBean(FxmlControllerFactory.class));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 800, 600);

        // Load custom theme overrides
        String customCss = getClass().getResource("/org/geantlr/theme.css").toExternalForm();
        scene.getStylesheets().add(customCss);

        stage.setTitle("geANTLR Editor Demo");
        stage.initStyle(StageStyle.EXTENDED);
        stage.setScene(scene);

        // addResizeListener(stage, scene);

        stage.show();
    }

    private void addResizeListener(Stage stage, Scene scene) {
        final int RESIZE_MARGIN = 5;

        scene.setOnMouseMoved(event -> {
            double x = event.getSceneX();
            double y = event.getSceneY();
            double width = scene.getWidth();
            double height = scene.getHeight();

            if (x < RESIZE_MARGIN && y < RESIZE_MARGIN) {
                scene.setCursor(Cursor.NW_RESIZE);
            } else if (x > width - RESIZE_MARGIN && y < RESIZE_MARGIN) {
                scene.setCursor(Cursor.NE_RESIZE);
            } else if (x < RESIZE_MARGIN && y > height - RESIZE_MARGIN) {
                scene.setCursor(Cursor.SW_RESIZE);
            } else if (x > width - RESIZE_MARGIN && y > height - RESIZE_MARGIN) {
                scene.setCursor(Cursor.SE_RESIZE);
            } else if (x < RESIZE_MARGIN) {
                scene.setCursor(Cursor.W_RESIZE);
            } else if (x > width - RESIZE_MARGIN) {
                scene.setCursor(Cursor.E_RESIZE);
            } else if (y < RESIZE_MARGIN) {
                scene.setCursor(Cursor.N_RESIZE);
            } else if (y > height - RESIZE_MARGIN) {
                scene.setCursor(Cursor.S_RESIZE);
            } else {
                scene.setCursor(Cursor.DEFAULT);
            }
        });

        // The following logic uses wrapper arrays because variables used in lambdas must be final
        final double[] startX = new double[1];
        final double[] startY = new double[1];
        final double[] startWidth = new double[1];
        final double[] startHeight = new double[1];
        final double[] stageX = new double[1];
        final double[] stageY = new double[1];

        scene.setOnMousePressed(event -> {
            startX[0] = event.getScreenX();
            startY[0] = event.getScreenY();
            startWidth[0] = stage.getWidth();
            startHeight[0] = stage.getHeight();
            stageX[0] = stage.getX();
            stageY[0] = stage.getY();
        });

        scene.setOnMouseDragged(event -> {
            Cursor cursor = scene.getCursor();
            if (cursor == Cursor.DEFAULT) {
                return;
            }

            double deltaX = event.getScreenX() - startX[0];
            double deltaY = event.getScreenY() - startY[0];
            double minWidth = 400; // minimum width
            double minHeight = 300; // minimum height

            if (cursor == Cursor.E_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.SE_RESIZE) {
                stage.setWidth(Math.max(minWidth, startWidth[0] + deltaX));
            }
            if (cursor == Cursor.S_RESIZE || cursor == Cursor.SW_RESIZE || cursor == Cursor.SE_RESIZE) {
                stage.setHeight(Math.max(minHeight, startHeight[0] + deltaY));
            }
            if (cursor == Cursor.W_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.SW_RESIZE) {
                double newWidth = startWidth[0] - deltaX;
                if (newWidth > minWidth) {
                    stage.setX(stageX[0] + deltaX);
                    stage.setWidth(newWidth);
                }
            }
            if (cursor == Cursor.N_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.NE_RESIZE) {
                double newHeight = startHeight[0] - deltaY;
                if (newHeight > minHeight) {
                    stage.setY(stageY[0] + deltaY);
                    stage.setHeight(newHeight);
                }
            }
        });
    }

    public static void main(String[] args) {
        System.setProperty("javafx.enablePreview", "true");
        launch();
    }
}
