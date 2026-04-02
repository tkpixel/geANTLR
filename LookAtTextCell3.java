import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;

public class LookAtTextCell3 extends Application {
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

            for (Node n : area.lookupAll(".vflow > .content > *")) {
                try {
                    java.lang.reflect.Method getIndexMethod = n.getClass().getMethod("getIndex");
                    Object indexVal = getIndexMethod.invoke(n);
                    System.out.println("Node " + n.getClass().getSimpleName() + " index=" + indexVal);
                } catch(Exception e) {}
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            javafx.application.Platform.exit();
        }
    }
}
