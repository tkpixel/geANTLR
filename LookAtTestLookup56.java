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

public class LookAtTestLookup56 extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            CodeArea area = new CodeArea();
            area.setText("Line 0\nLine 1\nLine 2");

            area.setSyntaxDecorator(new SyntaxDecorator() {
                @Override
                public RichParagraph createRichParagraph(CodeTextModel model, int index) {
                    String text = model.getPlainText(index);
                    RichParagraph.Builder builder = RichParagraph.builder();
                    builder.addWithStyleNames(text, "para-" + index);
                    return builder.build();
                }
                @Override
                public void handleChange(CodeTextModel model, TextPos start, TextPos end, int pc, int cc, int tl) {}
            });

            Scene scene = new Scene(area, 400, 300);
            stage.setScene(scene);
            stage.show();

            javafx.application.Platform.runLater(() -> {
                System.out.println("Looking up .para-1:");
                for (Node n : area.lookupAll(".para-1")) {
                    System.out.println("Found node: " + n.getClass().getName());
                    System.out.println("Parent is TextFlow? " + (n.getParent() instanceof TextFlow));
                    if (n.getParent() instanceof TextFlow flow) {
                        System.out.println("Flow text=" + flow.getChildren());
                    }
                }
                javafx.application.Platform.exit();
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
