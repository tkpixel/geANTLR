import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;
import javafx.scene.text.TextFlow;
import javafx.scene.Parent;
import javafx.geometry.Bounds;
import jfx.incubator.scene.control.richtext.TextPos;

public class LookAtTestLookup41 extends Application {
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
                                    TextPos pos = area.getTextPosition(pt.getX(), pt.getY());
                                    System.out.println("Cell y=" + pt.getY() + " mapped to index=" + (pos != null ? pos.index() : "null"));
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
