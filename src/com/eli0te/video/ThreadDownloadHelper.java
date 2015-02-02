package com.eli0te.video;


import com.eli0te.video.view.VideoOverviewController;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

/**
 * Created by eLi0tE on 16/01/15.
 * Utilisation du programme youtube-dl en python
 */
public class ThreadDownloadHelper implements Runnable {

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static String TEMP_FOLDER = "";

    //threadNumber
    private int threadNumber;
    public int getThreadNumber() {
        return threadNumber;
    }
    public void setThreadNumber(int threadNumber) {
        this.threadNumber = threadNumber;
    }

    //url
    private String url;
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    //outputPath
    private String outputPath;
    public String getOutputPath() {
        return outputPath;
    }
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    //audioOnly
    private boolean audioOnly;
    public boolean isAudioOnly() { return audioOnly; }
    public void setAudioOnly(boolean audioOnly) { this.audioOnly = audioOnly; }

    //videoController
    private VideoOverviewController controller;


    String cmd, cmd2;

    public ThreadDownloadHelper(String url, int threadNumber, boolean audioOnly, VideoOverviewController controller) {
        setThreadNumber(threadNumber);
        setOutputPath(controller.getDownloadPath());
        setUrl(url);
        setAudioOnly(audioOnly);
        this.controller = controller;
    }

    public void getAudio(String videoURL) throws Exception {

        String cmdYoutubeDl = "";
        String cmdFfmpeg = "";

        if (isWindowsOS()) {
            TEMP_FOLDER = System.getProperty("java.io.tmpdir") + "musicExtractorTemp\\";
            cmd += TEMP_FOLDER + "youtube-dl.exe";
            cmdYoutubeDl = TEMP_FOLDER + "youtube-dl" + String.valueOf(getThreadNumber()) + ".exe";
            cmdFfmpeg = TEMP_FOLDER + "ffmpeg" + String.valueOf(getThreadNumber()) + ".exe";
        } else {
            TEMP_FOLDER = System.getProperty("java.io.tmpdir") + "musicExtractorTemp/";
            cmd += TEMP_FOLDER + "youtube-dl";
            cmdYoutubeDl = TEMP_FOLDER + "youtube-dl" + String.valueOf(getThreadNumber());
            cmdFfmpeg = TEMP_FOLDER + "ffmpeg" + String.valueOf(getThreadNumber());
        }

        Process p;

        p = new ProcessBuilder(cmdYoutubeDl, "--get-filename", videoURL).start();
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String finalFileDir = outputPath + in.readLine().replace(".mp4",".mp3");

        String videoFileDirTemp = TEMP_FOLDER +"video_temp"+String.valueOf(getThreadNumber())+".mp4";
        String audioFileDirTemp = TEMP_FOLDER+"audio_temp"+String.valueOf(getThreadNumber())+".mp3";

        System.out.println("Debut du téléchargement de la vidéo du Thread n°"+String.valueOf(getThreadNumber()));
        p = new ProcessBuilder(cmdYoutubeDl,
                "-i",
                videoURL,
                "-o",
                videoFileDirTemp
        ).start();
        printProcessOutput(p);
        p.waitFor();
        System.out.println("Fin du téléchargement de la vidéo du Thread n°"+String.valueOf(getThreadNumber()));

        System.out.println("Début de la conversion audio du Thread n°"+String.valueOf(getThreadNumber()));
        p = new ProcessBuilder(cmdFfmpeg,
                "-i",
                videoFileDirTemp,
                audioFileDirTemp
        ).start();
        printProcessOutput(p);
        p.waitFor();
        System.out.println("fin de la conversion audio du Thread n°"+String.valueOf(getThreadNumber()));

        System.out.println("Thread n°"+String.valueOf(getThreadNumber())+" Nettoyage...");
        File fvideo = new File(videoFileDirTemp);
        File faudioTemp = new File(audioFileDirTemp);
        if(faudioTemp.exists()){
            faudioTemp.renameTo(new File(finalFileDir));
        }
        if(fvideo.exists()){
            fvideo.delete();
        }
        in.close();

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
     * Télécharge la vidéo de l'url passée en parametre vers le dossier également passé en parametre
     * @param videoURL : l'url de la vidéo a télécharger
     * @throws Exception : Si erreur dans youtube-dl.exe
     */
    private void getVideo(String videoURL) throws Exception {
        String cmdYoutubeDl, finalFileDir;

        if (isWindowsOS()) {
            cmdYoutubeDl = TEMP_FOLDER + "youtube-dl" + String.valueOf(threadNumber) + ".exe";
            finalFileDir = outputPath + "\\" + "%(title)s.%(ext)s";
        } else {
            cmdYoutubeDl = TEMP_FOLDER + "youtube-dl" + String.valueOf(threadNumber);
            finalFileDir = outputPath + "/" + "%(title)s.%(ext)s";
        }

        System.out.println("Debut du téléchargement de la vidéo du Thread n°"+String.valueOf(getThreadNumber()));
        Process p = new ProcessBuilder(cmdYoutubeDl,
                "-i",
                videoURL,
                "-o",
                finalFileDir
        ).start();
        printProcessOutput(p);
        p.waitFor();
        System.out.println("Fin du téléchargement de la vidéo du Thread n°"+String.valueOf(getThreadNumber()));
    }

    /**
     * Redirige la sortie de console du processus passé en parametre dans la sortie du logiciel
     * @param p : le processus dont la sortie doit être redirigée
     */
    private void printProcessOutput(Process p){
        BufferedReader in = new BufferedReader( new InputStreamReader(p.getInputStream()));
        String cmdOutput;
        try {
            while ( (cmdOutput = in.readLine()) != null ) { System.out.println(cmdOutput); }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isWindowsOS(){
        if ( OS.indexOf("win") >= 0 )
            return true;
        return false;
    }

    @Override
    public void run() {


        File youtubeDl, ffmpeg, destYoutubeDl, destFfmpeg;

        // Uses of the right library depending on OS
        if ( isWindowsOS() ) {
            TEMP_FOLDER = System.getProperty("java.io.tmpdir")+"musicExtractorTemp\\";
            outputPath += "\\";
            youtubeDl = new File("lib\\youtube-dl.exe");
            ffmpeg = new File("lib\\ffmpeg.exe");
            destYoutubeDl = new File(TEMP_FOLDER + "youtube-dl" + String.valueOf(getThreadNumber()) + ".exe");
            destFfmpeg = new File(TEMP_FOLDER + "ffmpeg" + String.valueOf(getThreadNumber()) + ".exe");
        } else {
            TEMP_FOLDER = System.getProperty("java.io.tmpdir")+"musicExtractorTemp/";
            outputPath += "/";
            youtubeDl = new File("lib/youtube-dl");
            ffmpeg = new File("lib/ffmpeg");
            destYoutubeDl = new File(TEMP_FOLDER + "youtube-dl" + String.valueOf(getThreadNumber()));
            destFfmpeg = new File(TEMP_FOLDER + "ffmpeg" + String.valueOf(getThreadNumber()));
        }

        File tempFolder = new File(TEMP_FOLDER);
        tempFolder.mkdirs();
        try {
            System.out.println("copie de youtubeDl" + getThreadNumber());
            Files.copy(youtubeDl.toPath(), destYoutubeDl.toPath());
            System.out.println("copie de ffmpeg" + getThreadNumber());
            Files.copy(ffmpeg.toPath(), destFfmpeg.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(isAudioOnly()){
            //Cas audio seul
            try {
                getAudio(getUrl());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            //Cas video
            try {
                getVideo(getUrl());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Suppression des fichiers (" + getThreadNumber() + ")");
        destYoutubeDl.delete();
        destFfmpeg.delete();
        tempFolder.delete();

    }
}
