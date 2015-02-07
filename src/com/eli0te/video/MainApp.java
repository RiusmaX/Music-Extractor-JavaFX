package com.eli0te.video;

import com.eli0te.video.model.Video;
import com.eli0te.video.view.VideoOverviewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Réalisation du tutoriel [http://code.makery.ch/java/javafx-8-tutorial-part1/] appliqué à la gestion des playlist
 *
 * Nous ne sommes pas responsables de l'utilisation du produit faite par l'utilisateur !!!
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private ObservableList<Video> videoData = FXCollections.observableArrayList();
    private VideoOverviewController controller;
    private int nbToDownload;

    private static final int downloaderPoolSize = 10;

    private static ExecutorService execSvcDl, execSvcInfo;

    /**
     *
     * @param s : String to show in dialog
     * @param dialogType : type of dialog you want to show
     */
    private void showDialog(String s, Alert.AlertType dialogType){
        Platform.runLater(() -> {
            Alert alert = new Alert(dialogType);
            alert.setTitle("Problème détecté !");
            alert.setHeaderText(null);
            alert.setContentText(s);

            alert.showAndWait();
        });
    }

    public void setVideoList(String url){

        if ( !url.contains("http") || !url.contains(".")){
            url = "https://www.youtube.com/results?search_query=" + url.replace(" ", "+");
        }


        final String finalUrl = url;

        Thread setVideoListThread = new Thread(() -> {
            try {
                execSvcInfo = Executors.newFixedThreadPool(1);
                Runnable info = new ThreadInformations(finalUrl, this);

                execSvcInfo.execute(info);
                execSvcInfo.shutdown();
                try {
                    execSvcInfo.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        });
        setVideoListThread.start();
    }

    public int getNbToDownload (){
        nbToDownload = 0;
        for( Video video : videoData ){
            if ( video.getToDownload() )
                nbToDownload++;
        }
        return nbToDownload;
    }

    public void addVideoToList(Video video){
        Platform.runLater(() -> videoData.add(video));
    }

    public void clearVideoToList(){
        Platform.runLater(videoData::clear);
    }

    public void download(){

        if ( !controller.getAudio() && !controller.getVideo() ) {
            showDialog("Veuillez sélectionner votre choix de téléchargement (Audio et/ou Vidéo) ! ", Alert.AlertType.WARNING);
            return;
        }

        Thread downloadThread = new Thread(() -> {
            execSvcDl = Executors.newFixedThreadPool(downloaderPoolSize);

            int j = 0;
            controller.resetProgressManager();
            for (int i = 0; i < videoData.size(); i++) {
                if (videoData.get(i).getToDownload()) {
                    Runnable dl = new VideoDownloader(videoData.get(i), controller, this, controller.getVideo(), controller.getAudio(), i, j);
                    execSvcDl.execute(dl);
                    j++;
                }
            }
            execSvcDl.shutdown();
            try {
                execSvcDl.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        downloadThread.start();
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Music Extractor");
        this.primaryStage.getIcons().add(new Image("file:resources/images/play.png"));
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

            primaryStage.setOnCloseRequest(windowEvent -> {
                File tmpFolder;
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    tmpFolder = new File(System.getProperty("java.io.tmpdir") + "musicExtractorTemp\\");
                } else {
                    tmpFolder = new File(System.getProperty("java.io.tmpdir") + "musicExtractorTemp/");
                }
                if (tmpFolder.exists()) {
                    System.out.println(tmpFolder + " -> EXIST !!!");
                    try {
                        deleteFile(tmpFolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Nettoyage dossier temporaire");
                Platform.exit();
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the video overview inside the root layout.
     */
    public void showVideoOverview() {
        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/VideoOverview.fxml"));
            AnchorPane videoOverview = loader.load();


            // Set video overview into the center of root layout.
            rootLayout.setCenter(videoOverview);

            // Give the controller access to the main app.
            controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public ObservableList<Video> getVideoData(){
        return videoData;
    }

    public void toggleAll() {
        for (int i = 0; i < videoData.size(); i++){
            if (controller.selectAllChecked())
                videoData.get(i).setToDownload(true);
            else
                videoData.get(i).setToDownload(false);
        }
    }

    public void deleteFile(File file) throws IOException{
        if ( file.isDirectory() ) {
            if ( file.list().length == 0 ) {
                file.delete();
                System.out.println("Suppression du dossier : " + file.getAbsolutePath());
            } else {
                String files[] = file.list();

                for ( String tmp : files ) {
                    File fileDelete = new File(file, tmp);
                    // Recursivity :
                    deleteFile(fileDelete);
                }

                if ( file.list().length == 0 ){
                    file.delete();
                    System.out.println("Suppression du dossier : " + file.getAbsolutePath());
                }
            }
        }else{
            //if file, then delete it
            file.delete();
            System.out.println("Suppression du fichier : " + file.getAbsolutePath());
        }
    }
}