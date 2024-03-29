package it.unipi.dii.client.controllers;


import it.unipi.dii.Libraries.Messages.*;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.Pair;

import java.util.Map.*;
import java.io.IOException;
import java.util.*;

public class ControllerAnalysisInterface {

    private ServerConnectionManager serverConnectionManager = ClientInterface.getServerConnectionManager();
    private ObservableList<XYChart.Series<String, Integer>> tagObservableMap;
    private User[] usersRankingArray;
    private String[] usersExpertsArray;
    private ObservableList<String> usersRankingList;
    private ObservableList<String> usersExpertsList;
    private ObservableList<Map<String, String>> hotTopicsObservableMap;
    private ObservableList<Map<String, String>> mostAnsweredTopUsersPostsObservableMap;
    private PageType lastPageVisited;

    @FXML
    private Label label_username;

    @FXML
    private Button button_back, button_search, button_update, button_search_experts;

    @FXML
    private TextField text_field_experts;

    @FXML
    private BarChart<String, Integer> bar_chart_mptags;

    @FXML
    private ListView<String> list_view_mptags_location, list_view_mpusers, list_view_experts;

    @FXML
    private TableView<Map<String, String>> table_view_hot_topics;

    @FXML
    private TableColumn<Map<String, String>, String> table_column_user, table_column_tags;

    @FXML
    private TableView<Map<String, String>> table_view_most_answered_top_users_posts;

    @FXML
    private TableColumn<Map<String, String>, String> table_column_username, table_column_title;

    public void initAnalyticsInterface(PageType pageType) throws IOException {
        lastPageVisited = pageType;
        initMPUsersList();
        initTagsChart();
        initExpertUsersList();
        initHotTopicsMap();
        iniMostAnsweredTopUsersPosts();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                         MOST POPULAR TAGS                                                      //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void initTagsChart() throws IOException {
        tagObservableMap = FXCollections.observableArrayList();
        bar_chart_mptags.setAnimated(false);
        serverConnectionManager.send(new MessageAnalyticMPTags(null));
    }

    public void resetTagList(){
        if(tagObservableMap != null) tagObservableMap.clear();
    }

    public void fillTagList(Map<String, Integer> tags){
        List<Integer> values = new ArrayList<>(tags.values());

        for (String tag:tags.keySet()) {
            XYChart.Series serie = new XYChart.Series();
            serie.getData().add(new XYChart.Data(tag, values.remove(0)));
            Platform.runLater(() -> {tagObservableMap.add(serie);});
        }

        Platform.runLater(() -> {bar_chart_mptags.setData(tagObservableMap);});
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

    public void eventSelectItem(MouseEvent mouseEvent) throws IOException {
        serverConnectionManager.send(
                new MessageGetUserData(
                        new User(null, list_view_mpusers.getSelectionModel().getSelectedItem(), null, null, null, null),
                        false,
                        PageType.ANALYSIS_INTERFACE
                )
        );
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

    public void resetExpertUsersList(){ if(usersExpertsList != null) usersExpertsList.clear(); }

    public void fillExpertUsersList(String[] users){
        usersExpertsArray = users;

        Platform.runLater(
                () -> {
                    usersExpertsList.addAll(Arrays.asList(usersExpertsArray));
                }
        );
    }

    public void eventSelectExpert(MouseEvent mouseEvent) throws IOException {
        serverConnectionManager.send(
                new MessageGetUserData(
                        new User(null, list_view_experts.getSelectionModel().getSelectedItem(), null, null, null, null),
                        false,
                        PageType.ANALYSIS_INTERFACE
                )
        );
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                         HOT TOPICS                                                             //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void initHotTopicsMap() throws IOException {
        hotTopicsObservableMap = FXCollections.observableArrayList();
        serverConnectionManager.send(new MessageAnalyticHotTopics(null));

        table_column_user.setCellValueFactory(new MapValueFactory("User"));
        table_column_tags.setCellValueFactory(new MapValueFactory("Tags"));
    }

    public void resetHotTopicsMap(){ if(hotTopicsObservableMap != null) hotTopicsObservableMap.clear(); }

    public void fillHotTopicsMap(HashMap<User, ArrayList<Pair<Post, Integer>>>  map){

        HashMap<User, ArrayList<Pair<Post, Integer>>> lista = map;

        Iterator it = lista.entrySet().iterator();

        while(it.hasNext()){
        //for (User u:map.keySet()) {
            Map<String, String> rawData = new HashMap<>();

            Entry item = (Entry) it.next();
            User u = (User) item.getKey();
            ArrayList<Pair<Post, Integer>> elem = (ArrayList<Pair<Post, Integer>>) item.getValue();
            String s = "";

            for (Pair<Post, Integer> e:elem) {
                s = s + e.getKey().getTags().toString() + "(" + e.getValue() + ") ";
            }

            rawData.put("User", u.getDisplayName());
            rawData.put("Tags", s);

            hotTopicsObservableMap.add(rawData);
        }

        table_view_hot_topics.setItems(hotTopicsObservableMap);
    }

    public void eventShowHotUser(MouseEvent mouseEvent) throws IOException {

        serverConnectionManager.send(
                new MessageGetUserData(
                        new User(null, table_view_hot_topics.getSelectionModel().getSelectedItem().get("User"), null, null, null, null),
                        false,
                        PageType.ANALYSIS_INTERFACE
                )
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                         MOST ANSWERED TOP USERS POSTS                                          //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void iniMostAnsweredTopUsersPosts() throws IOException {
        mostAnsweredTopUsersPostsObservableMap = FXCollections.observableArrayList();
        serverConnectionManager.send(new MessageGetTopUsersPosts(null));

        table_column_username.setCellValueFactory(new MapValueFactory("Username"));
        table_column_title.setCellValueFactory(new MapValueFactory("Title"));
    }

    public void resetMostAnsweredTopUsersPosts(){ if(mostAnsweredTopUsersPostsObservableMap != null) mostAnsweredTopUsersPostsObservableMap.clear(); }

    //TODO: fare questa query con un hashmap
    public void fillMostAnsweredTopUsersPosts(HashMap<User, ArrayList<Post>>  map){

        HashMap<User, ArrayList<Post>> lista = map;

        Iterator it = lista.entrySet().iterator();

        while(it.hasNext()){
            //for (User u:map.keySet()) {
            Map<String, String> rawData = new HashMap<>();

            Entry item = (Entry) it.next();
            User u = (User) item.getKey();
            ArrayList<Post> elem = (ArrayList<Post>) item.getValue();
            String s = "";

            rawData.put("Username", u.getDisplayName());
            rawData.put("Title", elem.get(0).getTitle());

            mostAnsweredTopUsersPostsObservableMap.add(rawData);
        }

        table_view_most_answered_top_users_posts.setItems(mostAnsweredTopUsersPostsObservableMap);
    }

    public void eventShowHotUserOrPost(MouseEvent mouseEvent) {

        //ClientInterface.switchScene(PageType.EXTERNAL_PROFILE);
        System.out.println(table_view_most_answered_top_users_posts.getSelectionModel().getSelectedItem());
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                         BUTTONS' METHODS                                                       //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @FXML
    public void eventButtonBack(ActionEvent actionEvent) throws IOException, InterruptedException {
        ClientInterface.switchScene(lastPageVisited);
    }

    @FXML
    public void eventButtonUpdate(ActionEvent actionEvent) throws IOException, InterruptedException {
        resetTagList();
        serverConnectionManager.send(new MessageAnalyticMPTags(null));
    }

    @FXML
    public void eventSearchExperts(ActionEvent actionEvent) throws IOException {
        resetExpertUsersList();
        serverConnectionManager.send(new MessageGetExpertsByTag(text_field_experts.getText(), null));
    }
}
