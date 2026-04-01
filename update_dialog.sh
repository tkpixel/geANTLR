# 1. Create MessageDialog.fxml
mkdir -p src/main/resources/org/geantlr/views
cat << 'INNER_EOF' > src/main/resources/org/geantlr/views/MessageDialog.fxml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.BorderPane?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.HeaderBar?>
<?import javafx.scene.layout.VBox?>

<BorderPane xmlns="http://javafx.com/javafx/25" xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="org.geantlr.views.MessageDialogController"
            styleClass="background">

    <top>
        <HeaderBar>
            <center>
                <Label fx:id="titleLabel" />
            </center>
        </HeaderBar>
    </top>

    <center>
        <VBox spacing="15">
            <padding>
                <Insets top="20" right="20" bottom="20" left="20"/>
            </padding>
            <Label fx:id="headerLabel" style="-fx-font-weight: bold; -fx-font-size: 14px;"/>
            <Label fx:id="contentLabel" wrapText="true" />
            <HBox alignment="CENTER_RIGHT" spacing="10">
                <Button text="OK" onAction="#close" styleClass="accent" />
            </HBox>
        </VBox>
    </center>
</BorderPane>
INNER_EOF

# 2. Create MessageDialogController.java
cat << 'INNER_EOF' > src/main/java/org/geantlr/views/MessageDialogController.java
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
INNER_EOF
