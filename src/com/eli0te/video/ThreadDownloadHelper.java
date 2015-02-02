package com.eli0te.video;

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

    String cmd, cmd2;

    public ThreadDownloadHelper(String url, String outputPath, int threadNumber, boolean audioOnly) {
        setThreadNumber(threadNumber);
        setOutputPath(outputPath);
        setUrl(url);
        setAudioOnly(audioOnly);
    }

    public void getAudio(String videoURL, String outputPath) throws Exception {

        String cmdYoutubeDl = TEMP_FOLDER+"youtube-dl("+String.valueOf(getThreadNumber())+").exe";
        String cmdFfmpeg = TEMP_FOLDER+"ffmpeg("+String.valueOf(getThreadNumber())+").exe";

        Process[] p = new Process[3];

        p[0] = new ProcessBuilder(cmdYoutubeDl,"--get-filename", videoURL).start();
        BufferedReader in = new BufferedReader( new InputStreamReader(p[0].getInputStream()) );

        String finalFileDir = outputPath + in.readLine().replace(".mp4",".mp3");

        String videoFileDirTemp = TEMP_FOLDER +"video_temp"+String.valueOf(getThreadNumber())+".mp4";
        String audioFileDirTemp = TEMP_FOLDER+"audio_temp"+String.valueOf(getThreadNumber())+".mp3";

        System.out.println("Debut du téléchargement de la vidéo du Thread n°"+String.valueOf(getThreadNumber()));
        p[1] = new ProcessBuilder(cmdYoutubeDl,
                "-i",
                videoURL,
                "-o",
                videoFileDirTemp
        ).start();
        printProcessOutput(p[1]);
        p[1].waitFor();
        System.out.println("Fin du téléchargement de la vidéo du Thread n°"+String.valueOf(getThreadNumber()));

        System.out.println("Début de la conversion audio du Thread n°"+String.valueOf(getThreadNumber()));
        p[2] = new ProcessBuilder(cmdFfmpeg,
                "-i",
                videoFileDirTemp,
                audioFileDirTemp
        ).start();
        printProcessOutput(p[2]);
        p[2].waitFor();
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
     * @param outputPath : le dossier de sortie
     * @throws Exception : Si erreur dans youtube-dl.exe
     */
    private void getVideo(String videoURL, String outputPath) throws Exception{
        String cmdYoutubeDl = TEMP_FOLDER+"youtube-dl("+String.valueOf(threadNumber)+").exe";

        Process[] p = new Process[2];

        String finalFileDir = outputPath +"\\"+ "%(title)s.%(ext)s";

        System.out.println("Debut du téléchargement de la vidéo du Thread n°"+String.valueOf(getThreadNumber()));
        p[1] = new ProcessBuilder(cmdYoutubeDl,
                "-i",
                videoURL,
                "-o",
                finalFileDir
        ).start();
        printProcessOutput(p[1]);
        p[1].waitFor();
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

        // Uses of the right library depending on OS
        if ( isWindowsOS() ) {
            TEMP_FOLDER = System.getProperty("java.io.tmpdir")+"musicExtractorTemp\\";
            outputPath += "\\";
        } else {
            TEMP_FOLDER = System.getProperty("java.io.tmpdir")+"musicExtractorTemp/";
            outputPath += "/";
        }


        File youtubeDl = new File("lib\\youtube-dl.exe");
        File ffmpeg = new File("lib\\ffmpeg.exe");
        File tempFolder = new File(TEMP_FOLDER);
        tempFolder.mkdirs();
        File destYoutubeDl = new File(TEMP_FOLDER+"youtube-dl("+String.valueOf(getThreadNumber())+").exe");
        File destFfmpeg = new File(TEMP_FOLDER+"ffmpeg("+String.valueOf(getThreadNumber())+").exe");
        try {
            System.out.println("copie de youtubeDl ("+getThreadNumber()+")");
            Files.copy(youtubeDl.toPath(), destYoutubeDl.toPath());
            System.out.println("copie de ffmpeg (" + getThreadNumber() + ")");
            Files.copy(ffmpeg.toPath(), destFfmpeg.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(isAudioOnly()){
            //Cas audio seul
            try {
                getAudio(getUrl(),getOutputPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            //Cas video
            try {
                getVideo(getUrl(), getOutputPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Suppression des fichiers ("+getThreadNumber()+")");
        destYoutubeDl.delete();
        destFfmpeg.delete();
        tempFolder.delete();

    }
}
