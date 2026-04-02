import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;
import javafx.scene.text.TextFlow;
import javafx.scene.Parent;
import javafx.geometry.Bounds;
import jfx.incubator.scene.control.richtext.TextPos;
import java.lang.reflect.Method;

public class LookAtTestLookup47 extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            CodeArea area = new CodeArea();
            area.setText("Line 0\nLine 1\nLine 2\nLine 3");
            Scene scene = new Scene(area, 400, 300);
            stage.setScene(scene);
            stage.show();

            // Wait for layout
            javafx.application.Platform.runLater(() -> {
                try {
                    System.out.println("Testing getTextPosition(x, y)...");
                    for (Node cell : area.lookupAll(".content > *")) {
                        if (cell instanceof Parent parentCell) {
                            TextFlow textFlow = null;
                            for (Node child : parentCell.getChildrenUnmodifiable()) {
                                if (child instanceof TextFlow flow) {
                                    textFlow = flow;
                                    break;
                                }
                            }
                            if (textFlow != null) {
                                Bounds bounds = cell.localToScreen(cell.getBoundsInLocal());
                                if (bounds != null) {
                                    javafx.geometry.Point2D pt = area.screenToLocal(bounds.getMinX() + 5, bounds.getMinY() + bounds.getHeight() / 2);

                                    // Try passing textFlow's local coordinate mapped to area!
                                    Bounds tfBounds = textFlow.localToScreen(textFlow.getBoundsInLocal());
                                    javafx.geometry.Point2D pt2 = area.screenToLocal(tfBounds.getMinX() + 5, tfBounds.getMinY() + tfBounds.getHeight() / 2);

                                    TextPos pos1 = area.getTextPosition(pt.getX(), pt.getY());
                                    TextPos pos2 = area.getTextPosition(pt2.getX(), pt2.getY());

                                    Method getIndexMethod = cell.getClass().getMethod("getIndex");
                                    int indexVal = (int) getIndexMethod.invoke(cell);

                                    System.out.println("Cell " + indexVal + " pt.Y=" + pt.getY() + " pos1=" + (pos1 != null ? pos1.index() : "null"));
                                    System.out.println("Cell " + indexVal + " pt2.Y=" + pt2.getY() + " pos2=" + (pos2 != null ? pos2.index() : "null"));
                                }
                            }
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
