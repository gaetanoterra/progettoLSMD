package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import server.*;
import middleware.*;

import java.util.ArrayList;

public class ControllerPost {

    @FXML
    private Label label_titolo_post;
    @FXML
    private ScrollPane scrollpane_body_post, scrollpane_answers_post;

    public void eventAnswerPostInterface(ActionEvent actionEvent) {
        Main.switchScene(PageType.CREATE_ANSWER);
    }

    //quando faccio back posso tornare sia in anonymousInterface sia in profileInterface, a seconda che il post aperto sia scritto da me o meno, aggiustare la funzione
    public void eventBackPostInterface(ActionEvent actionEvent) {
        Main.switchScene(PageType.ANONYMOUS_INTERFACE);
    }


    //da completare
    public void setPost(Post post){
        label_titolo_post.setText(post.getTitle());
        scrollpane_body_post.setContent(new Label(post.getBody()));

        VBox vbox = new VBox(1);

        for(int i = 0; i < post.getAnswers().size(); i++){

            HBox hboxVoti = new HBox(10);
            HBox hboxTags = new HBox(20);
            HBox hboxTotale = new HBox(60);
            VBox vboxTitoloTags = new VBox(20);

            //Label labelNumVoti = new Label(posts[i].get + " Voti");
            Label labelNumAnswer = new Label(posts[i].getAnswers().size() + " Answer");
            Label labelNumViews = new Label(posts[i].getViews() + " View");

            Label labelTag1 = new Label("");
            Label labelTag2 = new Label("");
            Label labelTag3 = new Label("");
            Label labelTag4 = new Label("");
            Label labelTag5 = new Label("");

            if(posts[i].getTags().get(0) != null)
                labelTag1.setText(posts[i].getTags().get(0));
            if(posts[i].getTags().get(1) != null)
                labelTag2.setText(posts[i].getTags().get(1));
            if(posts[i].getTags().get(2) != null)
                labelTag3.setText(posts[i].getTags().get(2));
            if(posts[i].getTags().get(3) != null)
                labelTag4.setText(posts[i].getTags().get(3));
            if(posts[i].getTags().get(4) != null)
                labelTag5.setText(posts[i].getTags().get(4));

            Separator separator = new Separator();

            Label labelStats = new Label("asked " + posts[i].getCreationDate() + " by " + posts[i].getOwnerUserId());
            labelStats.setPrefWidth(150);

            Label labelTitolo = new Label(posts[i].getTitle());
            labelTitolo.setFont(Font.font("Courier", 20));
            labelTitolo.setPrefWidth(320);
            labelTitolo.setAlignment(Pos.CENTER);
            labelTitolo.setOnMouseEntered(event -> {
                labelTitolo.setCursor(Cursor.HAND);
                labelTitolo.setTextFill(Color.BLUE);
            });
            labelTitolo.setOnMouseExited(event -> {
                labelTitolo.setTextFill(Color.BLACK);
            });
            labelTitolo.setOnMouseClicked(event -> {
                System.out.println("ho cliccato sul titolo");
            });

            hboxTags.setPrefWidth(320);
            hboxTags.setAlignment(Pos.CENTER);

            hboxVoti.setAlignment(Pos.CENTER_RIGHT);
            hboxVoti.setPrefWidth(150);

            hboxTotale.setAlignment(Pos.CENTER);

            vboxTitoloTags.setPrefWidth(320);
            vboxTitoloTags.setAlignment(Pos.CENTER);

            hboxVoti.getChildren().addAll(labelNumAnswer, labelNumViews);
            hboxTags.getChildren().addAll(labelTag1, labelTag2, labelTag3, labelTag4, labelTag5);
            vboxTitoloTags.getChildren().addAll(labelTitolo, hboxTags);
            hboxTotale.getChildren().addAll(hboxVoti, vboxTitoloTags, labelStats);

            vbox.getChildren().addAll(hboxTotale, separator);
            vbox.setAlignment(Pos.CENTER);

        }
        scrollpane_posts.setContent(vbox);
    }
}
