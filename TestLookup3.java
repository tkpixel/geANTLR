import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;
import java.lang.reflect.Method;
import javafx.scene.text.TextFlow;
import javafx.scene.Parent;

public class TestLookup3 extends Application {
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

            System.out.println("Finding TextFlow...");
            for (Node cell : area.lookupAll(".content > *")) {
                try {
                    Method getIndexMethod = cell.getClass().getMethod("getIndex");
                    Object indexVal = getIndexMethod.invoke(cell);

                    TextFlow textFlow = null;
                    if (cell instanceof Parent parentCell) {
                        for (Node child : parentCell.getChildrenUnmodifiable()) {
                            if (child instanceof TextFlow flow) {
                                textFlow = flow;
                                break;
                            }
                        }
                    }
                    System.out.println("Cell " + indexVal + " has TextFlow? " + (textFlow != null));
                } catch(Exception e) { }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            javafx.application.Platform.exit();
        }
    }
}
