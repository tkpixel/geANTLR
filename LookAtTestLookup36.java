import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;
import javafx.scene.text.TextFlow;
import javafx.scene.Parent;
import java.lang.reflect.Method;
import javafx.scene.text.CaretInfo;
import jfx.incubator.scene.control.richtext.SyntaxDecorator;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.TextPos;

public class LookAtTestLookup36 extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            CodeArea area = new CodeArea();
            area.setText("    FEHLER {\n    }");

            area.setSyntaxDecorator(new SyntaxDecorator() {
                @Override
                public RichParagraph createRichParagraph(CodeTextModel model, int index) {
                    String text = model.getPlainText(index);
                    RichParagraph.Builder builder = RichParagraph.builder();
                    builder.addSegment(text);
                    builder.addHighlight(0, text.length(), javafx.scene.paint.Color.RED);
                    return builder.build();
                }
                @Override
                public void handleChange(CodeTextModel model, TextPos start, TextPos end, int pc, int cc, int tl) {}
            });

            Scene scene = new Scene(area, 400, 300);
            stage.setScene(scene);
            stage.show();

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
                        CaretInfo caretInfo = textFlow.getLayoutInfo().caretInfoAt(4, true);
                        System.out.println("Cell " + indexVal + " caretInfo=" + caretInfo);
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
