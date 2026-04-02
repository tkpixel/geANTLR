import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;

public class LookAtTestLookup14 extends Application {
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

            System.out.println("Wait! The Y coordinates of `caretInfo` are LOCAL to the `TextFlow`! And `TextFlow` is localized in its `TextCell`!");
            System.out.println("Wait, if `TextFlow` is inside `TextCell`, its local bounds are Y=0 to Y=16.");
            System.out.println("And we mapped it using `textFlow.localToScene(localBounds)`!");
            System.out.println("Let's check if the problem is `closeCaret.getMinY() >= openCaret.getMaxY()` instead of `>`.");
            System.out.println("Wait, `minY2 >= maxY1 ? true` in my script. `minY2 > maxY1 ? true`.");
            System.out.println("BUT wait... What if `closeCaret` is ABOVE `openCaret`? E.g., user selects the `}` bracket! The `MatchedBracketsRecord` ALWAYS has `openIndex` as the SMALLER index!");
            System.out.println("YES! `EditorViewModel.java` lines:");
            System.out.println("`int openIdx = Math.min(charIdx, matchIdx);`");
            System.out.println("`int closeIdx = Math.max(charIdx, matchIdx);`");
            System.out.println("So `openIdx` is ALWAYS before `closeIdx`. So `openCaret` is ALWAYS above `closeCaret`!");
            System.out.println("Wait, then `closeCaret.getMinY() >= openCaret.getMaxY()` should ALWAYS be true for valid multi-line `{}`.");
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            javafx.application.Platform.exit();
        }
    }
}
