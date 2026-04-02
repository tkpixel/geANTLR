import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;

public class LookAtTestLookup9 extends Application {
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

            System.out.println("Wait! Look at `EditorViewController.java` lines 504:");
            System.out.println("if (closeCaret.getMinY() > openCaret.getMaxY()) { connectionLine.setVisible(true); }");
            System.out.println("What if openCaret and closeCaret are practically the same Y? No, they are different lines.");
            System.out.println("Wait! What if the close brace is `}` and the `openCaret` is `{`.");
            System.out.println("If it's consecutive lines, e.g. line 1: `FEHLER {` and line 2: `}`.");
            System.out.println("`openCaret.getMaxY()` is the BOTTOM of line 1.");
            System.out.println("`closeCaret.getMinY()` is the TOP of line 2.");
            System.out.println("What if they touch? Meaning `closeCaret.getMinY() == openCaret.getMaxY()`?");
            System.out.println("If they touch, `closeCaret.getMinY() > openCaret.getMaxY()` is FALSE! And line is set to visible=false!");
            System.out.println("And in most editors, lines have 0 spacing, so the bottom of line N is EXACTLY equal to the top of line N+1.");
            System.out.println("So `>` will be false! It should be `>=` !");
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            javafx.application.Platform.exit();
        }
    }
}
