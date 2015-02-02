package com.eli0te.video;

import com.eli0te.video.model.Video;
import com.eli0te.video.view.VideoOverviewController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Réalisation du tutoriel [http://code.makery.ch/java/javafx-8-tutorial-part1/] appliqué à la gestion des playlist
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private ObservableList<Video> videoData = FXCollections.observableArrayList();
    private VideoOverviewController controller;
    private ArrayList<HashMap<String, String>> videoList;
    private Helper helper;

    public MainApp(){


        helper = new Helper();
        videoList = new ArrayList();


    }

    public void setVideoList(String url){
        try {
            videoList =  helper.getInformation(url);
        } catch (Exception e){
            e.printStackTrace();
        }

        videoData.setAll(videoList.stream().map(Video::new).collect(Collectors.toList()));

    }

    public void download(){

        int nbToDownload = 0;

        for (int i = 0; i < videoData.size(); i++) {
            if (videoData.get(i).getToDownload()) {
                nbToDownload++;
            }
        }

        ExecutorService pool = Executors.newFixedThreadPool(nbToDownload);

        for (int i = 0; i < videoData.size(); i++) {
            if ( videoData.get(i).getToDownload() ) {
                System.out.println("Téléchargement de la vidéo : " + videoData.get(i).getVideoTitle());
                pool.submit(new ThreadDownloadHelper(videoData.get(i).getVideoUrl(),controller.getDownloadPath(),i,true));
            }
        }
        pool.shutdown();

        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Music Extractor");
        this.primaryStage.setMinHeight(768);
        this.primaryStage.setMinWidth(1024);

        initRootLayout();

        showVideoOverview();
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the person overview inside the root layout.
     */
    public void showVideoOverview() {
        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/VideoOverview.fxml"));
            AnchorPane videoOverview = loader.load();


            // Set person overview into the center of root layout.
            rootLayout.setCenter(videoOverview);

            // Give the controller access to the main app.
            controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public ObservableList<Video> getVideoData(){
        return videoData;
    }

    public void toogleAll() {
        for (int i = 0; i < videoData.size(); i++){
            videoData.get(i).setToDownload(!videoData.get(i).getToDownload());
        }

    }
}