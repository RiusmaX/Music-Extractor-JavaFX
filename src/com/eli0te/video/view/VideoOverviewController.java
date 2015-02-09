package com.eli0te.video.view;

import com.eli0te.video.MainApp;
import com.eli0te.video.model.Video;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sun.applet.Main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


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
    @FXML
    private CheckBox downloadVideo;
    @FXML
    private CheckBox downloadAudio;


    private Double[] tabProgressManager;
    private String videoUrl;
    private MainApp mainApp;

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
        selectAll.setOnAction(event -> mainApp.toggleAll());

        // Validate button handler
        validate.setOnAction(event -> {
            mainApp.setVideoList(url.getText());

        });

        videoTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
                public void handle(MouseEvent click) {
                if (click.getClickCount() == 2) {
                    Video videoSelected = videoTable.getSelectionModel().getSelectedItem();
                    for (int i=0;i<videoTable.getItems().size();i++){
                        videoTable.getItems().get(i).setToDownload(false);
                    }
                    videoSelected.setToDownload(true);
                    mainApp.download();
                }
            }
        });



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

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("resources/img/play.png");
        InputStream is2 = this.getClass().getClassLoader().getResourceAsStream("resources/img/pause.png");
        Image playImg = new Image(is, 20, 20, false, false);
        Image pauseImg = new Image(is2, 20, 20, false, false);

        try {
            is.close();
            is2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        playButton.setGraphic(new ImageView(playImg));
        pauseButton.setGraphic(new ImageView(pauseImg));

//        playButton.setOnAction(event -> mainApp.previewVideo());
//        pauseButton.setOnAction(event -> mainApp.download());
    }

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
            videoUploader.setText(video.getVideoUploader());
            videoUrl = video.getVideoUrl();

            if ( videoUrl.contains("youtube") ){
                videoEmbed.setVisible(true);
                videoEmbed.getEngine().load(videoUrl.replace("watch?v=", "embed/") + "?controls=1&showinfo=0&modestbranding=1&autohide=1");
                videoThumbnail.setVisible(false);
            } else {
                videoThumbnail.setVisible(true);
                videoThumbnail.setImage(new Image(video.getVideoThumbnail(), 640, 360, false, false));
                videoEmbed.setVisible(false);
            }

            //         pauseButton.setVisible(true);
            //         playButton.setVisible(true);

        } else {
            // Person is null, remove all the text.
            videoTitle.setText("Bienvenue sur Music Extractor");
            videoDescription.setText("Entrez le lien dans la barre en haut plus cliquez sur valider ! ");
            videoDuration.setText("");
            // Insérer le logo du logiciel ici !
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("resources/img/logo.png");
            videoThumbnail.setImage(new Image(is, 512, 512, false, false));
            videoThumbnail.setVisible(true);
            videoUploader.setText("World Of Code");
            videoEmbed.setVisible(false);
            playButton.setVisible(false);
            pauseButton.setVisible(false);
        }
    }

    public boolean getVideo(){
        return downloadVideo.isSelected();
    }

    public boolean getAudio(){
        return downloadAudio.isSelected();
    }

    public void resetProgressManager() {
        tabProgressManager = new Double[mainApp.getNbToDownload()];
        for ( int i = 0; i < mainApp.getNbToDownload(); i++ )
            tabProgressManager[i] = 0.0;
    }

    public void setMainApp(MainApp mainApp){
        this.mainApp = mainApp;

        videoTable.setItems(mainApp.getVideoData());
    }

    public boolean selectAllChecked() {
        return selectAll.isSelected();
    }

    public synchronized void updateProgress(Double value, int videoId) {
        tabProgressManager[videoId] = value;
        Double res2 = 0.0;
        for (int i = 0; i < mainApp.getNbToDownload(); i++) {
            res2 += tabProgressManager[i];
        }
        res2 = (res2 / mainApp.getNbToDownload()) / 100;
        progress.setProgress(res2);
    }

    public void onEnter(){
        mainApp.setVideoList(url.getText());
    }

    public String getDownloadPath(){
        return downloadPath.getText();
    }
}
