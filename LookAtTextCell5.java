import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;

public class LookAtTextCell5 extends Application {
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

            for (Node content : area.lookupAll(".content")) {
                if (content instanceof javafx.scene.Parent pcontent) {
                    for (Node cell : pcontent.getChildrenUnmodifiable()) {
                        try {
                            java.lang.reflect.Method getIndexMethod = cell.getClass().getMethod("getIndex");
                            Object indexVal = getIndexMethod.invoke(cell);
                            System.out.println("Node " + cell.getClass().getSimpleName() + " index=" + indexVal);
                        } catch(Exception e) {
                            System.out.println("Node " + cell.getClass().getSimpleName() + " no index");
                        }
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            javafx.application.Platform.exit();
        }
    }
}
