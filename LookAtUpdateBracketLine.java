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
import javafx.geometry.Bounds;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import jfx.incubator.scene.control.richtext.TextPos;

public class LookAtUpdateBracketLine extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            CodeArea area = new CodeArea();
            area.setText("    FEHLER {\n    }");
            Pane overlay = new Pane();
            StackPane root = new StackPane(area, overlay);
            Scene scene = new Scene(root, 400, 300);
            stage.setScene(scene);
            stage.show();

            TextPos openPos = TextPos.ofLeading(0, 11); // `{`
            TextPos closePos = TextPos.ofLeading(1, 4); // `}`
            TextPos indentPos = TextPos.ofLeading(0, 4); // `F`

            Rectangle2D indentCaret = getCaretBounds(area, overlay, indentPos);
            Rectangle2D openCaret = getCaretBounds(area, overlay, openPos);
            Rectangle2D closeCaret = getCaretBounds(area, overlay, closePos);

            System.out.println("indentCaret=" + indentCaret);
            System.out.println("openCaret=" + openCaret);
            System.out.println("closeCaret=" + closeCaret);

            if (indentCaret != null && openCaret != null && closeCaret != null) {
                System.out.println("minY=" + closeCaret.getMinY() + " maxY=" + openCaret.getMaxY());
                System.out.println("Condition: " + (closeCaret.getMinY() >= openCaret.getMaxY()));
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            javafx.application.Platform.exit();
        }
    }

    private javafx.geometry.Rectangle2D getCaretBounds(CodeArea editorCodeArea, Pane bracketLineOverlay, TextPos pos) {
        if (pos == null) return null;
        int targetPara = pos.index();
        int charIdx = pos.offset();
        for (javafx.scene.Node cell : editorCodeArea.lookupAll(".content > *")) {
            boolean isMatch = false;
            try {
                java.lang.reflect.Method getIndexMethod = cell.getClass().getMethod("getIndex");
                Object indexVal = getIndexMethod.invoke(cell);
                if (indexVal instanceof Integer && ((Integer) indexVal) == targetPara) {
                    isMatch = true;
                }
            } catch (Exception e) {}

            if (isMatch) {
                javafx.scene.text.TextFlow textFlow = null;
                if (cell instanceof javafx.scene.Parent parentCell) {
                    for (javafx.scene.Node child : parentCell.getChildrenUnmodifiable()) {
                        if (child instanceof javafx.scene.text.TextFlow flow) {
                            textFlow = flow;
                            break;
                        }
                    }
                }
                if (textFlow != null) {
                    javafx.scene.text.LayoutInfo layoutInfo = textFlow.getLayoutInfo();
                    if (layoutInfo != null) {
                        try {
                            javafx.scene.text.CaretInfo caretInfo = layoutInfo.caretInfoAt(charIdx, true);
                            if (caretInfo != null && caretInfo.getSegmentCount() > 0) {
                                javafx.geometry.Rectangle2D localCaret = caretInfo.getSegmentAt(0);
                                javafx.geometry.Bounds localBounds = new javafx.geometry.BoundingBox(localCaret.getMinX(), localCaret.getMinY(), localCaret.getWidth(), localCaret.getHeight());
                                javafx.geometry.Bounds overlayBounds = bracketLineOverlay.sceneToLocal(textFlow.localToScene(localBounds));
                                return new javafx.geometry.Rectangle2D(overlayBounds.getMinX(), overlayBounds.getMinY(), overlayBounds.getWidth(), overlayBounds.getHeight());
                            }
                        } catch (Exception ex) {
                            System.out.println("Exception in caretInfoAt: " + ex);
                        }
                    } else {
                        System.out.println("layoutInfo is null for para " + targetPara);
                    }
                } else {
                    System.out.println("textFlow is null for para " + targetPara);
                }
                break;
            }
        }
        return null;
    }
}
