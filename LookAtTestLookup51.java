import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.TextPos;

public class LookAtTestLookup51 extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            CodeArea area = new CodeArea();
            area.setText("Line 0\nLine 1\nLine 2\nLine 3\nLine 4\nLine 5");
            Scene scene = new Scene(area, 400, 300);
            stage.setScene(scene);
            stage.show();

            // Wait for layout
            javafx.application.Platform.runLater(() -> {
                try {
                    System.out.println("Testing getTextPosition multiple points...");
                    for (int y = 0; y < 100; y += 10) {
                        TextPos pos = area.getTextPosition(10, y);
                        System.out.println("y=" + y + " pos=" + (pos != null ? pos.index() : "null"));
                    }
                    javafx.application.Platform.exit();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
