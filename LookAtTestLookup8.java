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

public class LookAtTestLookup8 extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            CodeArea area = new CodeArea();
            area.setText("    FEHLER {\n}\nFEHLER {\n}");
            Scene scene = new Scene(area, 400, 300);
            stage.setScene(scene);
            stage.show();

            System.out.println("Checking caret info at 4...");
            for (Node cell : area.lookupAll(".content > *")) {
                try {
                    Method getIndexMethod = cell.getClass().getMethod("getIndex");
                    int indexVal = (int) getIndexMethod.invoke(cell);

                    TextFlow textFlow = null;
                    if (cell instanceof Parent parentCell) {
                        for (Node child : parentCell.getChildrenUnmodifiable()) {
                            if (child instanceof TextFlow flow) {
                                textFlow = flow;
                                break;
                            }
                        }
                    }
                    if (textFlow != null && textFlow.getLayoutInfo() != null) {
                        try {
                            CaretInfo caretInfo = textFlow.getLayoutInfo().caretInfoAt(4, true);
                            System.out.println("Cell " + indexVal + " caretInfo(4)=" + caretInfo);
                        } catch (Exception ex) {
                            System.out.println("Cell " + indexVal + " Error: " + ex.getMessage());
                        }
                    }
                } catch(Exception e) { }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            javafx.application.Platform.exit();
        }
    }
}
