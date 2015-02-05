package com.eli0te.video.view;

import com.eli0te.video.MainApp;
import com.eli0te.video.model.Video;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

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
    private ProgressBar progress;
    @FXML
    private CheckBox selectAll;
    @FXML
    private Button playButton;
    @FXML
    private Button pauseButton;
    @FXML
    private WebView videoEmbed;



    private Double progressManager;
    private String videoUrl;
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
            videoThumbnail.setImage(new Image(video.getVideoThumbnail(), 640, 360, false, false));
            videoUploader.setText(video.getVideoUploader());
            videoUrl = video.getVideoUrl();
            videoEmbed.setVisible(true);
            videoEmbed.getEngine().load(getVideoUrl().replace("watch?v=", "embed/") + "?controls=1&showinfo=0&modestbranding=1");
            videoThumbnail.setVisible(false);
   //         pauseButton.setVisible(true);
   //         playButton.setVisible(true);
/*
            try {
                resource = new URL(video.getVideoUrl());
                media = new Media(resource.toString());
                player = new MediaPlayer(media);
                player.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
*/
        } else {
            // Person is null, remove all the text.
            videoTitle.setText("Bienvenue sur Music Extractor");
            videoDescription.setText("Entrez le lien dans la barre en haut plus cliquez sur valider ! ");
            videoDuration.setText("");
            // Insérer le logo du logiciel ici !
            videoThumbnail.setImage(new Image("file:resources/img/logo.png", 512, 512, false, false));
            videoUploader.setText("World Of Code");
            videoEmbed.setVisible(false);
            playButton.setVisible(false);
            pauseButton.setVisible(false);
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

        // SelectAll checkbox handler
        selectAll.setOnAction(event -> mainApp.toogleAll());

        // Validate button handler
        validate.setOnAction(event -> mainApp.setVideoList(url.getText()));

        // Download button handler
        download.setOnAction(event -> mainApp.download());

        // ChangeDirectory button handler
        changeDirectory.setOnAction(actionEvent -> {
            try {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File selectedDirectory = directoryChooser.showDialog(mainApp.getPrimaryStage());
                downloadPath.setText(selectedDirectory.getAbsolutePath());
            } catch (NullPointerException e) {
            }
        });

        Image playImg = new Image("file:resources/img/play.png", 20, 20, false, false);
        Image pauseImg = new Image("file:resources/img/pause.png", 20, 20, false, false);

        playButton.setGraphic(new ImageView(playImg));
        pauseButton.setGraphic(new ImageView(pauseImg));

//        playButton.setOnAction(event -> mainApp.previewVideo());
//        pauseButton.setOnAction(event -> mainApp.download());
    }

    /*public static void updateProgress(double progressPercentage){
        progress.setProgress(progressPercentage);
    }*/

    public void setMainApp(MainApp mainApp){
        this.mainApp = mainApp;

        videoTable.setItems(mainApp.getVideoData());
    }

    public boolean selectAllChecked() {
        return selectAll.isSelected();
    }

    public void updateProgress(Double value) {
        progressManager += (value / mainApp.getNbToDownload());
        progress.setProgress(progressManager);
    }

    public String getDownloadPath(){
        return downloadPath.getText();
    }

    public String getVideoUrl() { return videoUrl; }
}
