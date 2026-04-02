import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;
import java.lang.reflect.Method;

public class TestLookup2 extends Application {
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

            System.out.println("Testing getIndex reflection...");
            for (Node cell : area.lookupAll(".content > *")) {
                try {
                    Method getIndexMethod = cell.getClass().getMethod("getIndex");
                    Object indexVal = getIndexMethod.invoke(cell);
                    System.out.println("Cell " + cell.getClass().getName() + " -> index = " + indexVal);
                } catch(Exception e) {
                    System.out.println("Cell " + cell.getClass().getName() + " -> no getIndex: " + e.getClass().getName());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            javafx.application.Platform.exit();
        }
    }
}
