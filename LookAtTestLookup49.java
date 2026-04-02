import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;
import java.lang.reflect.Method;

public class LookAtTestLookup49 extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            CodeArea area = new CodeArea();
            area.setText("FEHLER {\n}\nFEHLER {\n}");
            Scene scene = new Scene(area, 400, 300);
            stage.setScene(scene);
            stage.show();

            // Wait for layout
            javafx.application.Platform.runLater(() -> {
                try {
                    System.out.println("Testing Cell string representation...");
                    for (Node cell : area.lookupAll(".content > *")) {
                        System.out.println("cell.toString() = " + cell.toString());
                        System.out.println("cell.getId() = " + cell.getId());
                        System.out.println("cell.getUserData() = " + cell.getUserData());
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
