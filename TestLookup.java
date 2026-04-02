import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;

public class TestLookup extends Application {
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

            System.out.println("Testing .content > *");
            int count1 = area.lookupAll(".content > *").size();
            System.out.println("Count: " + count1);

            System.out.println("Testing iteration of .content");
            int count2 = 0;
            for (Node content : area.lookupAll(".content")) {
                if (content instanceof javafx.scene.Parent p) {
                    count2 += p.getChildrenUnmodifiable().size();
                }
            }
            System.out.println("Count: " + count2);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            javafx.application.Platform.exit();
        }
    }
}
