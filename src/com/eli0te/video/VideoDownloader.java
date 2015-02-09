package com.eli0te.video;


import com.eli0te.video.model.Video;
import com.eli0te.video.view.VideoOverviewController;

import java.io.*;

/**
 * Created by eLi0tE on 16/01/15.
 * Utilisation du programme youtube-dl en python
 */
public class VideoDownloader implements Runnable {

    private static String OS = System.getProperty("os.name").toLowerCase();

    private Video video;

    private int videoNumber;
    private int videoId;

    private Process p;

    private boolean dlAudio;
    private boolean dlVideo;

    //videoController
    private VideoOverviewController controller;

    // Main App
    MainApp mainApp;


    String cmd;
    // VidéoNumber, numéro de la vidéo dans le liste de toutes les vidéos et videoId numéro de la vidéo dans la liste des vidéo à DL
    public VideoDownloader(Video video, VideoOverviewController controller, MainApp mainApp, boolean dlVideo, boolean dlAudio, int videoNumber, int videoId) {
        this.dlAudio = dlAudio;
        this.dlVideo = dlVideo;
        this.videoNumber = videoNumber;
        this.video = video;
        this.controller = controller;
        this.mainApp = mainApp;
        this.videoId = videoId;
    }

    public void download() throws Exception {

        String finalFileDir = controller.getDownloadPath();
        if (isWindowsOS()) finalFileDir += "\\";
        else finalFileDir += "/";
        finalFileDir += video.getVideoTitle() + ".mp3";

        String videoFileDirTemp = mainApp.getTEMP_FOLDER() + "video_temp" + String.valueOf(videoNumber) + ".mp4";
        String audioFileDirTemp = mainApp.getTEMP_FOLDER() + "audio_temp" + String.valueOf(videoNumber) + ".mp3";

        System.out.println("Debut du téléchargement de la vidéo : " + video.getVideoTitle());

        p = new ProcessBuilder(mainApp.getYoutubeDlPathOut(),
                "-i",
                video.getVideoUrl(),
                "-o",
                videoFileDirTemp
        ).start();

        BufferedReader in = new BufferedReader( new InputStreamReader(p.getInputStream()) );

        String cmdOutput;
        String s;

        // Parsing download progress :
        while ( (cmdOutput = in.readLine()) != null ) {
            if ( cmdOutput.contains("[download] ") && cmdOutput.contains("%")  ) {
                s = cmdOutput.substring("[download] ".length(), cmdOutput.indexOf('%'));
                if ( s.contains(".") ) // Exclude last doubling 100%
                    controller.updateProgress( Double.parseDouble(s), videoId );
            }
        }
        in.close();

        System.out.println("Fin du téléchargement de la vidéo  : " + video.getVideoTitle());

        if (dlAudio) {
            System.out.println("Début de la conversion audio de la video : " + video.getVideoTitle());
            p = new ProcessBuilder(mainApp.getFfmpegPathOut(),
                    "-i",
                    videoFileDirTemp,
                    audioFileDirTemp
            ).start();
            printProcessOutput(p);
            p.waitFor();
            System.out.println("Fin de la conversion audio de la vidéo : " + video.getVideoTitle());
        }

        System.out.println("Nettoyage des fichiers de la vidéo : " + video.getVideoTitle());
        File fvideo = new File(videoFileDirTemp);
        if (dlAudio) {
            File faudioTemp = new File(audioFileDirTemp);
            if (faudioTemp.exists()) {
                faudioTemp.renameTo(new File(finalFileDir));
            }
        }
        if (dlVideo){
            fvideo.renameTo(new File(finalFileDir.replace("mp3", "mp4")));
        }
        if(fvideo.exists()){
            fvideo.delete();
        }

    }

    private static String RemoveIllegalPathCharacters(String path)
    {
        return path.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    private static String RemoveIllegalPathCharactersForRename(String path)
    {
        return path.replaceAll("[^a-zA-Z0-9.-]", " ");
    }

    private String getFormatDuration(int seconds){
        String formatedDuration = "";
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        if (hours > 0) {
            formatedDuration += String.valueOf(hours) + ":";
        }

        return formatedDuration += minutes + ":" + seconds;

    }

    /**
     * Redirige la sortie de console du processus passé en parametre dans la sortie du logiciel
     * @param p : le processus dont la sortie doit être redirigée
     */
    private void printProcessOutput(Process p) {
        BufferedReader in = new BufferedReader( new InputStreamReader(p.getInputStream()));
        String cmdOutput;
        try {
            while ((cmdOutput = in.readLine()) != null) {
                System.out.println(cmdOutput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isWindowsOS(){
        if (OS.contains("win"))
            return true;
        return false;
    }


    @Override
    public void run() {
        try {
            download();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
