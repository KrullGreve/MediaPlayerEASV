package your.package.name;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.MediaView;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;

public class HelloController{

    @FXML
    private MediaView mvMainImage;

    @FXML
    private Label myDuration;

    @FXML
    private Slider mySliderDuration;

    @FXML
    private Button bntShuffel;

    @FXML
    private Button bntStop;

    @FXML
    private Button bntBack;

    @FXML
    private Button bntPlay;

    @FXML
    private Button bntNext;

    @FXML
    private Button bntRepeat;

    @FXML
    private Button bntMute;

    @FXML
    private Slider mySliderVolume;

    @FXML
    private ListView<String> lvPlayList;

    @FXML
    private ListView<String> lvPlayLists;

    @FXML
    private ComboBox<String> cbSearchBar;

    @FXML
    private Label myLabelTitel;

    @FXML
    private Label myLabelCurrentSong;

    @FXML
    private ImageView ivIcon;
}