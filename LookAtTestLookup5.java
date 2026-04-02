import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;

public class LookAtTestLookup5 extends Application {
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

            System.out.println("Wait, does `charIdx <= flowText.length()` check in `getCaretBounds` fail?");
            System.out.println("Wait, I removed that check!");
            System.out.println("Let's look at `EditorViewController.getCaretBounds` in `src/main/java/org/geantlr/views/EditorViewController.java`");
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            javafx.application.Platform.exit();
        }
    }
}
