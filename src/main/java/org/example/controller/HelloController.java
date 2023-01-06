package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
public class HelloController {

    @FXML
    private TextField usernameTextField;

    @FXML
    protected void onGoButtonClick(ActionEvent event) throws IOException {
        String username = usernameTextField.getText();
        if (username.isBlank())
            username = "user";

        FXMLLoader loader = new FXMLLoader(HelloController.class.getResource("/org.example/main-scene.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setUsername(username);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }
}
