import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;


public class Main extends Application{

    private static AnchorPane root;

    static List<AnchorPane> grid = new ArrayList<AnchorPane>();

    private static int idx_cur = 0;

    @Override
    public void start (Stage primaryStage) throws Exception {
        try {

            root = (AnchorPane) FXMLLoader.load(getClass().getResource("anchor.fxml"));

            grid.add((AnchorPane) FXMLLoader.load(getClass().getResource("anonymousInterface.fxml")));
            grid.add((AnchorPane) FXMLLoader.load(getClass().getResource("signin.fxml")));
            grid.add((AnchorPane) FXMLLoader.load(getClass().getResource("signup.fxml")));
            grid.add((AnchorPane) FXMLLoader.load(getClass().getResource("profileInterface.fxml")));
            grid.add((AnchorPane)FXMLLoader.load(getClass().getResource("anonymousInterface.fxml")));
            grid.add((AnchorPane)FXMLLoader.load(getClass().getResource("message.fxml")));

            root.getChildren().add(grid.get(0));
            Scene scene = new Scene(root);

            primaryStage.setScene(scene);
            primaryStage.show();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void switchScene(int idx){

        root.getChildren().remove(grid.get(idx_cur));
        root.getChildren().add(grid.get(idx));
        idx_cur = idx;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
