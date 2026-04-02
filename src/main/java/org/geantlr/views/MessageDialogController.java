package org.geantlr.views;

import io.micronaut.context.annotation.Prototype;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

@Prototype
public class MessageDialogController {

    @FXML private Label titleLabel;
    @FXML private Label headerLabel;
    @FXML private Label contentLabel;

    public void setDialogInfo(String title, String header, String content) {
        titleLabel.setText(title);
        headerLabel.setText(header);
        contentLabel.setText(content);
    }

    @FXML
    private void close() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }
}
