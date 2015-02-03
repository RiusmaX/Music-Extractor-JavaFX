package com.eli0te.video;

import com.eli0te.video.model.Video;
import com.eli0te.video.view.VideoOverviewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Réalisation du tutoriel [http://code.makery.ch/java/javafx-8-tutorial-part1/] appliqué à la gestion des playlist
 *
 * Nous ne sommes pas responsable de l'utilisation du produit faite par l'utilisateur !!!
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private ObservableList<Video> videoData = FXCollections.observableArrayList();
    private VideoOverviewController controller;

    private static final int downloaderPoolSize = 10;

    private static ExecutorService execSvcDl, execSvcInfo;

    private static List<VideoDownloader> managedEngines;

    public static List<VideoDownloader> getManagedEngines() {
        return managedEngines;
    }

    public MainApp() {
        managedEngines = new ArrayList<>();
    }

    public void setVideoList(String url){
        try {
            execSvcInfo = Executors.newFixedThreadPool(1);
            Runnable info = new ThreadInformations(url, this);

            execSvcInfo.execute(info);
            execSvcInfo.shutdown();
            try {
                execSvcInfo.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // videoInfo = new ThreadInformations(url, this);
            //    videoList =  helper.getInformation(url);
        } catch (Exception e){
            e.printStackTrace();
        }

    // videoData.setAll(videoList.stream().map(Video::new).collect(Collectors.toList()));

    }

    public void addVideoToList(Video video){
        videoData.add(video);
    }

    public void download(){

        execSvcDl = Executors.newFixedThreadPool(downloaderPoolSize);

        for (int i = 0; i < videoData.size(); i++) {
            if (videoData.get(i).getToDownload()) {
                Runnable dl = new VideoDownloader(videoData.get(i), controller, true, i);
                execSvcDl.execute(dl);
            }
        }
        execSvcDl.shutdown();
        try {
            execSvcDl.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
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

            primaryStage.setOnCloseRequest(windowEvent -> {
                File tmpFolder;
                if ( System.getProperty("os.name").toLowerCase().indexOf("win") >= 0 ) {
                    tmpFolder = new File(System.getProperty("java.io.tmpdir") + "musicExtractorTemp\\");
                } else {
                    tmpFolder = new File(System.getProperty("java.io.tmpdir") + "musicExtractorTemp/");
                }
                if ( tmpFolder.exists() ){
                    System.out.println(tmpFolder + " -> EXIST !!!");
                    try {
                        delete(tmpFolder);
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
            if (controller.selectAllChecked())
                videoData.get(i).setToDownload(true);
            else
                videoData.get(i).setToDownload(false);
        }
    }

    private void delete(File file) throws IOException{
        if ( file.isDirectory() ) {
            if ( file.list().length == 0 ) {
                file.delete();
                System.out.println("Suppression du dossier : " + file.getAbsolutePath());
            } else {
                String files[] = file.list();

                for ( String tmp : files ) {
                    File fileDelete = new File(file, tmp);
                    // Recursivity :
                    delete(fileDelete);
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