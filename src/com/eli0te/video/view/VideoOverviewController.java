package com.eli0te.video.view;

import com.eli0te.video.MainApp;
import com.eli0te.video.model.Video;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;

import java.io.File;


/**
 * Created by ~eLi0tE~ on 23/01/2015.
 */
public class VideoOverviewController {
    @FXML
    private TableView<Video> videoTable;
    @FXML
    private TableColumn<Video, String> titleColumn;
    @FXML
    private TableColumn<Video, Boolean> checkboxColumn;

    @FXML
    private Label videoTitle;
    @FXML
    private TextArea videoDescription;
    @FXML
    private Label videoDuration;
    @FXML
    private Label videoUploader;
    @FXML
    private ImageView videoThumbnail;
    @FXML
    private TextField url;
    @FXML
    private Button validate;
    @FXML
    private Button download;
    @FXML
    private TextField downloadPath;
    @FXML
    private Button changeDirectory;
    @FXML
    private static ProgressBar progress;
    @FXML
    private CheckBox selectAll;


    private MainApp mainApp;

    /**
     * Fills all text fields to show details about the person.
     * If the specified person is null, all text fields are cleared.
     *
     * @param video the video or null
     */
    private void setVideoDetails(Video video) {
        if (video != null) {
            // Fill the labels with info from the video object.
            videoTitle.setText(video.getVideoTitle());
            videoDescription.setText(video.getVideoDescription());
            videoDuration.setText(video.getVideoDuration());
            videoThumbnail.setImage(new Image(video.getVideoThumbnail()));
            videoUploader.setText(video.getVideoUploader());
        } else {
            // Person is null, remove all the text.
            videoTitle.setText("Bienvenue sur Music Extractor");
            videoDescription.setText("Entrez le lien dans la barre en haut plus cliquez sur valider ! ");
            videoDuration.setText("");
            // Insérer le logo du logiciel ici !
            videoThumbnail.setImage(new Image("https://lh4.googleusercontent.com/-4QDxins1Vgw/AAAAAAAAAAI/AAAAAAAAlN0/jL-wM5HIagA/s120-c/photo.jpg"));
            videoUploader.setText("");
        }
    }

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public VideoOverviewController() {
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize the person table with the two columns.
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().videoTitleProperty());
        checkboxColumn.setCellValueFactory(cellData -> cellData.getValue().toDownloadProperty());
        checkboxColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkboxColumn));
        checkboxColumn.setEditable(true);

        url.setText("https://www.youtube.com/watch?v=S2bjqrRbNW4&list=RDS2bjqrRbNW4#t=0");
        downloadPath.setText(System.getProperty("user.home"));
        downloadPath.setDisable(true);

        // Clear video details
        setVideoDetails(null);

        // Listen for selection changes and show the person details when changed.
        videoTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> setVideoDetails(newValue)
        );
        videoTable.setEditable(true);

        selectAll.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mainApp.toogleAll();
            }
        });

        validate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mainApp.setVideoList(url.getText());
            }
        });

        download.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //TODO : Envoyer la liste de vidéos au lieu d'envoyer l'url qui pourrait être effacée !
                mainApp.download();
            }
        });

        changeDirectory.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File selectedDirectory = directoryChooser.showDialog(mainApp.getPrimaryStage());
                downloadPath.setText(selectedDirectory.getAbsolutePath());

            }
        });
    }

    /*public static void updateProgress(double progressPercentage){
        progress.setProgress(progressPercentage);
    }*/

    public void setMainApp(MainApp mainApp){
        this.mainApp = mainApp;

        videoTable.setItems(mainApp.getVideoData());
    }

    public String getDownloadPath(){
        return downloadPath.getText();
    }
}
