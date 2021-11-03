package it.unipi.dii.client.controllers;


import it.unipi.dii.Libraries.Messages.*;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ControllerAnalysisInterface {

    //TODO: quando premo sul bottone per accedere alla pagina delle statistiche devo lanciare la query per popolare la pie chart dei most popular tags
    //TODO: quando premo update aggiorno la pie chart dei most popular tags

    private ServerConnectionManager serverConnectionManager = ClientInterface.getServerConnectionManager();
    private ObservableMap<String, Integer> tagObservableMap;
    private ObservableList<String> tagLocationObservableList;
    private User[] usersRankingArray;
    private String[] usersExpertsArray;
    private ObservableList<String> usersRankingList;
    private ObservableList<String> usersExpertsList;
    private PageType lastPageVisited;

    @FXML
    private Label label_username;

    @FXML
    private Button button_back, button_search, button_update, button_search_experts;

    @FXML
    private TextField text_field_location, text_field_experts;

    @FXML
    private BarChart<String, Integer> bar_chart_mptags;

    @FXML
    private ListView list_view_mptags_location, list_view_mpusers, list_view_experts;

    //TODO: metodo da chiamare allo switch in questa interfaccia
    public void initAnalyticsInterface(PageType pageType) throws IOException {
        lastPageVisited = pageType;
        initMPUsersList();
        //initTagsChart();
        initTagsLocationList();
        initExpertUsersList();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                         MOST POPULAR TAGS                                                      //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void initTagsChart(){
        bar_chart_mptags.setData((ObservableList<XYChart.Series<String, Integer>>) tagObservableMap);
    }

    public void resetTagList(){
        if(tagObservableMap != null) tagObservableMap.clear();
    }

    public void fillTagList(Map<String, Integer> tags){
        Platform.runLater(() -> { tagObservableMap.putAll(tags); });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                         MOST POPULAR TAGS PER LOCATION                                         //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void initTagsLocationList(){
        tagLocationObservableList = FXCollections.observableArrayList();
        list_view_mptags_location.setItems(tagLocationObservableList);
    }

    public void resetTagLocationList(){
        if(tagLocationObservableList != null) tagLocationObservableList.clear();
    }

    //Platform.runLater is needed to avoid an IllegalStateException
    public void fillTagLocationList(String[] tags){
            Platform.runLater(() -> { tagLocationObservableList.addAll(tags); });
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                         MOST POPULAR USERS                                                     //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void initMPUsersList() throws IOException {
        usersRankingList = FXCollections.observableArrayList();
        serverConnectionManager.send(new MessageAnalyticUserRanking(null));
        list_view_mpusers.setItems(usersRankingList);
    }

    public void resetMPUsersList(){ if(usersRankingList != null) usersRankingList.removeAll(); }

    public void fillMPUsersList(User[] users){ 
        usersRankingArray = users;

        Platform.runLater(
                () -> {
                    for (User u: usersRankingArray) {
                        usersRankingList.add(u.getDisplayName());
                    }
                }
        );
    }

    //TODO: necessario metodo che selezionando un utente della lista apre il suo profilo (va modificata la switchScene aggiungendo l'utente su cui switchare, servirà anche per aprire un Post specifico)
    public void eventSelectItem(MouseEvent mouseEvent) throws IOException {
        serverConnectionManager.send(new MessageGetUserData(new ArrayList<>(List.of(new User(null, list_view_mpusers.getSelectionModel().getSelectedItem().toString(), null, null, null, null))), false));
    }

    public void loadExternalProfile(User user) {
        Platform.runLater(
                () -> { ClientInterface.switchScene(PageType.EXTERNAL_PROFILE); });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                         EXPERTS BY TAG                                                         //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void initExpertUsersList() throws IOException {
        usersExpertsList = FXCollections.observableArrayList();
        list_view_experts.setItems(usersExpertsList);
    }

    public void resetExpertUsersList(){ if(usersExpertsList != null) usersExpertsList.removeAll(); }

    public void fillExpertUsersList(String[] users){
        usersExpertsArray = users;

        Platform.runLater(
                () -> {
                    for (String u: usersExpertsArray) {
                        usersExpertsList.add(u);
                    }
                }
        );
    }

    //TODO: necessario metodo che selezionando un utente della lista apre il suo profilo (va modificata la switchScene aggiungendo l'utente su cui switchare, servirà anche per aprire un Post specifico)
    public void eventSelectExpert(MouseEvent mouseEvent) {

        //ClientInterface.switchScene(PageType.EXTERNAL_PROFILE);
        System.out.println(list_view_experts.getSelectionModel().getSelectedItem());
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                         BUTTONS' METHODS                                                       //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void eventButtonBack(ActionEvent actionEvent) throws IOException, InterruptedException {
        ClientInterface.switchScene(lastPageVisited);
    }

    public void eventButtonUpdate(ActionEvent actionEvent) throws IOException, InterruptedException {
        resetTagList();
        serverConnectionManager.send(new MessageAnalyticMPTags(null));
    }

    public void eventButtonSearch(ActionEvent actionEvent) throws  IOException, InterruptedException {
        resetTagLocationList();
        serverConnectionManager.send(new MessageAnalyticMPTagsLocation(text_field_location.getText(), 10, null));
    }

    public void eventSearchExperts(ActionEvent actionEvent) throws IOException {
        resetExpertUsersList();
        serverConnectionManager.send(new MessageGetExpertsByTag(text_field_experts.getText(), null));
    }
}
