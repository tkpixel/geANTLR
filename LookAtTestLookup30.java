import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.TextPos;
import java.lang.reflect.Method;

public class LookAtTestLookup30 extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            CodeArea area = new CodeArea();
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<100; i++) sb.append("Line ").append(i).append("\n");
            area.setText(sb.toString());
            Scene scene = new Scene(area, 400, 300);
            stage.setScene(scene);
            stage.show();

            // Wait for layout
            javafx.application.Platform.runLater(() -> {
                try {
                    // Scroll down
                    area.select(TextPos.ofLeading(90, 0));

                    javafx.application.Platform.runLater(() -> {
                        System.out.println("After select:");
                        for (Node cell : area.lookupAll(".content > *")) {
                            try {
                                Method getIndexMethod = cell.getClass().getMethod("getIndex");
                                Object indexVal = getIndexMethod.invoke(cell);
                                System.out.println("Cell " + cell.getClass().getSimpleName() + " index=" + indexVal);
                            } catch(Exception e) {}
                        }
                        javafx.application.Platform.exit();
                    });
                } catch(Exception e) {}
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
