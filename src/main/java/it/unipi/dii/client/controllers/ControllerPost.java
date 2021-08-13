package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.client.ClientInterface;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;

public class ControllerPost {

    @FXML
    private Text text_title;
    @FXML
    private ScrollPane scrollpane_body_post, scrollpane_answers_post;

    public void eventAnswerPostInterface(ActionEvent actionEvent) throws IOException {
        ClientInterface.switchScene(PageType.CREATE_ANSWER);
    }

    //quando faccio back posso tornare sia in anonymousInterface sia in profileInterface, a seconda che il post aperto sia scritto da me o meno, aggiustare la funzione
    public void eventBackPostInterface(ActionEvent actionEvent) throws IOException {
        ClientInterface.switchScene(PageType.POSTSEARCHINTERFACE);
    }

    public void setPost(Post post){
        text_title.setText(post.gettitle());
        scrollpane_body_post.setContent(new Label(post.getBody()));

        VBox vbox = new VBox(1);

        for(int i = 0; i < post.getAnswers().size(); i++){

            Answer answer = post.getAnswers().get(i);

            VBox vboxVoti = new VBox(10);
            HBox hboxButtons = new HBox(20);
            HBox hboxTotale = new HBox(60);

            Text textBody = new Text(answer.getBody());
            textBody.setWrappingWidth(320);

            Label labelNumVoti = new Label(String.valueOf(answer.getScore()));

            Button votoPos = new Button("+");
            Button votoNeg = new Button("-");

            hboxButtons.getChildren().addAll(votoPos, votoNeg);

            vboxVoti.getChildren().addAll(labelNumVoti, hboxButtons);

            Separator separator = new Separator();

            Label labelStats = new Label("answered " + answer.getCreationDate() + " by " + answer.getOwnerUserName());
            labelStats.setPrefWidth(150);

            hboxTotale.setAlignment(Pos.CENTER);

            hboxTotale.getChildren().addAll(vboxVoti, textBody, labelStats);

            vbox.getChildren().addAll(hboxTotale, separator);
            vbox.setAlignment(Pos.CENTER);

        }
        scrollpane_answers_post.setContent(vbox);
    }
}
