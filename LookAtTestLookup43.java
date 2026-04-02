import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;
import java.util.Map;

public class LookAtTestLookup43 extends Application {
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

            javafx.application.Platform.runLater(() -> {
                try {
                    System.out.println("Properties...");
                    for (Node cell : area.lookupAll(".content > *")) {
                        System.out.println("Cell " + cell.getClass().getSimpleName() + " properties:");
                        for(Map.Entry<Object, Object> entry : cell.getProperties().entrySet()) {
                            System.out.println("  " + entry.getKey() + " = " + entry.getValue());
                        }
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
