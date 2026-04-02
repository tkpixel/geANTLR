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

public class LookAtTestLookup10 extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            CodeArea area = new CodeArea();
            area.setText("    FEHLER {\n    }");
            Scene scene = new Scene(area, 400, 300);
            stage.setScene(scene);
            stage.show();

            double maxY1 = -1, minY2 = -1;

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
                        CaretInfo caretInfo = textFlow.getLayoutInfo().caretInfoAt(0, true);
                        javafx.geometry.Bounds bounds = textFlow.localToScene(new javafx.geometry.BoundingBox(caretInfo.getMinX(), caretInfo.getMinY(), caretInfo.getMaxX() - caretInfo.getMinX(), caretInfo.getMaxY() - caretInfo.getMinY()));
                        if (indexVal == 0) maxY1 = bounds.getMaxY();
                        if (indexVal == 1) minY2 = bounds.getMinY();
                    }
                } catch(Exception e) { }
            }
            System.out.println("maxY1=" + maxY1 + " minY2=" + minY2);
            System.out.println("minY2 > maxY1 ? " + (minY2 > maxY1));
            System.out.println("minY2 >= maxY1 ? " + (minY2 >= maxY1));
            System.out.println("Wait! For `FEHLER {` and `}` with lines in between, `minY2` could be even slightly less if there are rounding errors? No, they should be well separated.");
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            javafx.application.Platform.exit();
        }
    }
}
