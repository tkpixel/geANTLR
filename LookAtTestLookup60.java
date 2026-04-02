import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;
import javafx.scene.text.TextFlow;
import javafx.scene.Parent;
import javafx.geometry.Bounds;
import java.util.ArrayList;
import java.util.List;

public class LookAtTestLookup60 extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            CodeArea area = new CodeArea();
            area.setText("Line 0\nLine 1\nLine 2\nLine 3\nLine 4");
            Scene scene = new Scene(area, 400, 300);
            stage.setScene(scene);
            stage.show();

            // Move caret to line 2
            area.select(jfx.incubator.scene.control.richtext.TextPos.ofLeading(2, 0));

            javafx.application.Platform.runLater(() -> {
                try {
                    System.out.println("Finding Caret Y...");
                    Node caret = area.lookup(".caret");
                    if (caret != null) {
                        Bounds caretBounds = caret.localToScene(caret.getBoundsInLocal());
                        double caretY = caretBounds.getCenterY();
                        System.out.println("Caret center Y: " + caretY);

                        List<TextFlow> flows = new ArrayList<>();
                        for (Node cell : area.lookupAll(".content > *")) {
                            if (cell instanceof Parent parentCell) {
                                for (Node child : parentCell.getChildrenUnmodifiable()) {
                                    if (child instanceof TextFlow flow) {
                                        flows.add(flow);
                                        break;
                                    }
                                }
                            }
                        }

                        flows.sort((a, b) -> Double.compare(a.localToScene(a.getBoundsInLocal()).getMinY(), b.localToScene(b.getBoundsInLocal()).getMinY()));

                        int caretFlowIndex = -1;
                        for (int i=0; i<flows.size(); i++) {
                            TextFlow f = flows.get(i);
                            Bounds b = f.localToScene(f.getBoundsInLocal());
                            System.out.println("Flow " + i + " minY=" + b.getMinY() + " maxY=" + b.getMaxY() + " text=" + f.getChildren());
                            if (caretY >= b.getMinY() && caretY <= b.getMaxY()) {
                                caretFlowIndex = i;
                            }
                        }

                        System.out.println("Caret Flow Index: " + caretFlowIndex);

                        int caretParaIndex = area.getCaretPosition().index();
                        System.out.println("Caret Para Index: " + caretParaIndex);

                        for (int i=0; i<flows.size(); i++) {
                            int paraIndex = caretParaIndex - (caretFlowIndex - i);
                            System.out.println("Flow " + i + " -> Paragraph " + paraIndex);
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
