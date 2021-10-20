package it.unipi.dii.client.controllers;


import it.unipi.dii.Libraries.Messages.MessageAnalyticMPTags;
import it.unipi.dii.Libraries.Messages.MessageAnalyticMPTagsLocation;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import javax.swing.*;
import java.io.IOException;
import java.util.Map;

public class ControllerAnalysisInterface {

    //TODO: quando premo sul bottone per accedere alla pagina delle statistiche devo lanciare la query per popolare la pie chart dei most popular tags
    //TODO: quando premo update aggiorno la pie chart dei most popular tags

    private ServerConnectionManager serverConnectionManager = ClientInterface.getServerConnectionManager();
    private ObservableMap<String, Integer> tagObservableMap;
    private ObservableList<String> tagLocationObservableList;

    @FXML
    private Label label_username;

    @FXML
    private Button button_back, button_search, button_update;

    @FXML
    private TextField text_field_location;

    @FXML
    private PieChart pie_chart_mptags, pie_chart_mptags_location;

    @FXML
    private TableView table_view_mpusers;

    //metodi most popular tags section
    public void resetTagList(){ this.tagObservableMap.clear(); }

    public void fillTagList(Map<String, Integer> tags){ tagObservableMap.putAll(tags); }

    //metodi most popular tags per location section
    public void resetTagLocationList(){ this.tagLocationObservableList.removeAll(); }

    public void fillTagLocationList(String[] tags){ tagLocationObservableList.setAll(tags); }

    //metodi dei Buttons
    //button used to go back to the profile interface
    public void eventButtonBack(ActionEvent actionEvent) throws IOException, InterruptedException {
        ClientInterface.switchScene(PageType.PROFILE_INTERFACE);
    }

    public void eventButtonUpdate(ActionEvent actionEvent) throws IOException, InterruptedException {
        resetTagList();
        serverConnectionManager.send(new MessageAnalyticMPTags(null));
    }

    public void eventButtonSearch(ActionEvent actionEvent) throws  IOException, InterruptedException {
        resetTagLocationList();
        serverConnectionManager.send(new MessageAnalyticMPTagsLocation(text_field_location.getText(), 10, null));
    }

}
