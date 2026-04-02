import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;
import javafx.scene.text.TextFlow;
import javafx.scene.Parent;
import java.lang.reflect.Method;
import javafx.geometry.Rectangle2D;
import javafx.scene.text.CaretInfo;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;

public class LookAtTestLookup17 extends Application {
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

            System.out.println("What about `bracketLineOverlay.sceneToLocal(textFlow.localToScene(localBounds))`?");
            System.out.println("Wait! I am doing `bracketLineOverlay.sceneToLocal(...)`. But `bracketLineOverlay` is NOT rendered yet or its parent hierarchy isn't set up yet?");
            System.out.println("BUT this is `Platform.runLater()`. So it should be.");
            System.out.println("Wait! Look at `EditorViewController.java` lines 582-584:");
            System.out.println("```");
            System.out.println("javafx.geometry.Bounds localBounds = new javafx.geometry.BoundingBox(localCaret.getMinX(), localCaret.getMinY(), localCaret.getWidth(), localCaret.getHeight());");
            System.out.println("javafx.geometry.Bounds overlayBounds = bracketLineOverlay.sceneToLocal(textFlow.localToScene(localBounds));");
            System.out.println("```");
            System.out.println("What if `bracketLineOverlay.getScene() == null`?");
            System.out.println("Then `sceneToLocal` returns NULL or throws NPE!");
            System.out.println("Let's check if `bracketLineOverlay.getScene()` is null.");

            // Dummy test
            javafx.scene.layout.Pane p = new javafx.scene.layout.Pane();
            try {
                Bounds b = p.sceneToLocal(new BoundingBox(0,0,10,10));
                System.out.println("sceneToLocal: " + b);
            } catch(Exception e) {
                System.out.println("sceneToLocal without scene threw: " + e.getClass().getName());
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            javafx.application.Platform.exit();
        }
    }
}
