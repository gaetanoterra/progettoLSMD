package client;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import server.*;
import middleware.*;

import java.io.IOException;

//classe preposta a gestire l'interfaccia da cui si visualizzano i Post
public class ControllerAnonymousInterface {

    private ClientServerManager clm;

    private static Parameter searchMethod = Parameter.Date;
    private static String filter = "interesting";
    private static Post[] posts;
    private static Post last_post_seen;

    @FXML
    private Label label_username_anonymous_interface;
    @FXML
    private Button signin_button, signup_button, profile_button, sort_interesting, sort_hot, sort_week, sort_month;
    @FXML
    private RadioButton radio_button_date, radio_button_tags, radio_button_username, radio_button_text;
    @FXML
    private TextField textfield_search;
    @FXML
    private ScrollPane scrollpane_posts;

    public ControllerAnonymousInterface(ClientServerManager clm){
        this.clm = clm;
    }

    //imposto cosa posso utilizzare e cosa no a seconda che l'utente sia loggato o meno
    public void setLoggedInterface(String username){
        label_username_anonymous_interface.setText(username);
        signin_button.setDisable(true);
        signup_button.setDisable(true);
        profile_button.setDisable(false);
    }

    //rendo di nuovo disponibili i bottoni e scollego l'interfaccia dall'utente
    public void resetInterface(){
        label_username_anonymous_interface.setText("Anonymous");
        signin_button.setDisable(false);
        signup_button.setDisable(false);
        profile_button.setDisable(true);
    }

    //metodo per inserire i post nel panello
    public void fillPostPane() {
        //ordino i post in base al sort
        ordinaPost();

        VBox vbox = new VBox(1);

        for(int i = 0; i < posts.length; i++){

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
            labelTitolo.setId("titolo_post:" + i);
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
                String titolo = labelTitolo.getId();
                String[] t = titolo.split(":");
                last_post_seen = posts[Integer.parseInt(t[1])];
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

    //ordino i post in base al sort
    private void ordinaPost() {
        if(filter.equals("interesting")){

        }
        else if(filter.equals("hot")){

        }
        else if(filter.equals("week")){

        }
        else if(filter.equals("month")){

        }
    }

    //funzione chiamata da ClientServerManager per fornire i post da visualizzare
    public void setPosts(Post[] post) {
        posts = post;
        fillPostPane();
    }

    public void eventButtonSignIn(ActionEvent event){
        Main.switchScene(PageType.SIGN_IN);
    }

    public void eventButtonSignUp(ActionEvent event){
        Main.switchScene(PageType.SIGN_UP);
    }

    public void eventButtonProfile(ActionEvent actionEvent) {
        Main.switchScene(PageType.PROFILE_INTERFACE);
        Main.getControllerProfileInterface().lockTextArea();
    }

    public void eventSetSearchMethod(ActionEvent actionEvent) {
        if(radio_button_date.isSelected())
            searchMethod = Parameter.Date;
        if(radio_button_tags.isSelected())
            searchMethod = Parameter.Tags;
        if(radio_button_text.isSelected())
            searchMethod = Parameter.Text;
        if(radio_button_username.isSelected())
            searchMethod = Parameter.Username;

    }

    //event che invia la richiesta al ClientManager in base al parametro di ricerca
    public void eventButtonSearch(ActionEvent actionEvent) throws IOException, InterruptedException {
        if(searchMethod == Parameter.Date)
            clm.send(new MessageGetPostByParameter(Parameter.Date, textfield_search.getText(), null));

        else if(searchMethod == Parameter.Tags)
            clm.send(new MessageGetPostByParameter(Parameter.Tags, textfield_search.getText(), null));

        else if(searchMethod == Parameter.Text)
            clm.send(new MessageGetPostByParameter(Parameter.Text, textfield_search.getText(), null));

        else if(searchMethod == Parameter.Username)
            clm.send(new MessageGetPostByParameter(Parameter.Username, textfield_search.getText(), null));

        while(ClientServerManager.getInAttesa())
            wait();

        fillPostPane();
        ClientServerManager.setInAttesa(true);
    }

    //event per impostare il valore del filtro
    public void eventSetFilter(ActionEvent actionEvent) {
        if(sort_interesting.isPressed())
            filter = "interesting";
        else if(sort_hot.isPressed())
            filter = "hot";
        else if(sort_week.isPressed())
            filter = "week";
        else if(sort_month.isPressed())
            filter = "month";
    }

    public static Post lastPostSeen(){
        return last_post_seen;
    }
}
