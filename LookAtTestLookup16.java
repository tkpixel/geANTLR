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

public class LookAtTestLookup16 extends Application {
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

            System.out.println("What does `caretInfo.getSegmentCount()` return when caret is at index 0? Or end?");
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
                        System.out.println("Cell " + indexVal + " caretInfo(0) segmentCount=" + caretInfo.getSegmentCount());
                        if (caretInfo.getSegmentCount() == 0) {
                            System.out.println("Wait! If segment count is 0, we do `if (caretInfo != null && caretInfo.getSegmentCount() > 0)`.");
                            System.out.println("If it is 0, it skips `return` and goes to `break;`! And `getCaretBounds` returns `null`!");
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
