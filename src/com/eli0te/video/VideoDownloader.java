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

    public static String TEMP_FOLDER = "";

    private boolean dlAudio;
    private boolean dlVideo;

    //videoController
    private VideoOverviewController controller;

    // Main App
    MainApp main;


    String cmd;
    // VidéoNumber, numéro de la vidéo dans le liste de toutes les vidéos et videoId numéro de la vidéo dans la liste des vidéo à DL
    public VideoDownloader(Video video, VideoOverviewController controller, MainApp main, boolean dlVideo, boolean dlAudio, int videoNumber, int videoId) {
        this.dlAudio = dlAudio;
        this.dlVideo = dlVideo;
        this.videoNumber = videoNumber;
        this.video = video;
        this.controller = controller;
        this.main = main;
        this.videoId = videoId;
    }

    public void download() throws Exception {

        String cmdYoutubeDl = "";
        String cmdFfmpeg = "";

        if (isWindowsOS()) {
            TEMP_FOLDER = System.getProperty("java.io.tmpdir") + "musicExtractorTemp\\";
            cmd += TEMP_FOLDER + "youtube-dl.exe";
            cmdYoutubeDl = TEMP_FOLDER + "youtube-dl" + String.valueOf(videoNumber) + ".exe";
            cmdFfmpeg = TEMP_FOLDER + "ffmpeg" + String.valueOf(videoNumber) + ".exe";
        } else {
            TEMP_FOLDER = System.getProperty("java.io.tmpdir") + "musicExtractorTemp/";
            cmd += TEMP_FOLDER + "youtube-dl";
            cmdYoutubeDl = TEMP_FOLDER + "youtube-dl" + String.valueOf(videoNumber);
            cmdFfmpeg = TEMP_FOLDER + "ffmpeg" + String.valueOf(videoNumber);
            p = new ProcessBuilder("chmod", "a+x",cmdFfmpeg).start();
        }

        String finalFileDir = controller.getDownloadPath();
        if (isWindowsOS()) finalFileDir += "\\";
        else finalFileDir += "/";
        finalFileDir += video.getVideoTitle() + ".mp3";

        String videoFileDirTemp = TEMP_FOLDER + "video_temp" + String.valueOf(videoNumber) + ".mp4";
        String audioFileDirTemp = TEMP_FOLDER + "audio_temp" + String.valueOf(videoNumber) + ".mp3";

        System.out.println("Debut du téléchargement de la vidéo : " + video.getVideoTitle());


        try {
            p = new ProcessBuilder(cmdYoutubeDl,
                    "-i",
                    video.getVideoUrl(),
                    "-o",
                    videoFileDirTemp
            ).start();
        } catch (IOException e){
            p = new ProcessBuilder("chmod", "a+x",cmdYoutubeDl).start();
            p = new ProcessBuilder(cmdYoutubeDl,
                    "-i",
                    video.getVideoUrl(),
                    "-o",
                    videoFileDirTemp
            ).start();
        }



        BufferedReader in = new BufferedReader( new InputStreamReader(p.getInputStream()) );

        String cmdOutput;
        String s;

        // Parsing download progress :
        while ( (cmdOutput = in.readLine()) != null ) {
            if ( cmdOutput.contains("[download] ") && cmdOutput.contains("%")  ) {
                s = cmdOutput.substring("[download] ".length(), cmdOutput.indexOf('%'));
                if ( s.contains(".") ) // Exclude last doubling 100%
                    controller.updateProgress(Double.parseDouble(s), videoId );
            }
        }
        in.close();

        System.out.println("Fin du téléchargement de la vidéo  : " + video.getVideoTitle());

        if (dlAudio) {
            System.out.println("Début de la conversion audio de la video : " + video.getVideoTitle());
            p = new ProcessBuilder(cmdFfmpeg,
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
        } else
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



        InputStream isYoutubeDl, isffmpeg;
        FileOutputStream fosYoutubeDl = null;
        FileOutputStream fosffmpeg = null;

        // Uses of the right library depending on OS
        if ( isWindowsOS() ) {
            TEMP_FOLDER = System.getProperty("java.io.tmpdir")+"musicExtractorTemp\\";

            File tempFolder = new File(TEMP_FOLDER);
            tempFolder.mkdirs();

            isYoutubeDl = this.getClass().getClassLoader().getResourceAsStream("lib/youtube-dl.exe");
            try {
                fosYoutubeDl = new FileOutputStream(TEMP_FOLDER + "youtube-dl" + videoNumber + ".exe");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            int c;
            try {
                while ((c = isYoutubeDl.read()) != -1)
                    fosYoutubeDl.write(c);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if ( dlAudio ) {
                isffmpeg = this.getClass().getClassLoader().getResourceAsStream("lib/ffmpeg.exe");
                try {
                    fosffmpeg = new FileOutputStream(TEMP_FOLDER + "ffmpeg" + videoNumber + ".exe");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    while ((c = isffmpeg.read()) != -1)
                        fosffmpeg.write(c);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            TEMP_FOLDER = System.getProperty("java.io.tmpdir")+"musicExtractorTemp/";

            File tempFolder = new File(TEMP_FOLDER);
            tempFolder.mkdirs();

            isYoutubeDl = this.getClass().getClassLoader().getResourceAsStream("lib/youtube-dl");
            try {
                fosYoutubeDl = new FileOutputStream(TEMP_FOLDER + "youtube-dl" + videoNumber);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            int c;
            try {
                while ((c = isYoutubeDl.read()) != -1)
                    fosYoutubeDl.write(c);
                fosYoutubeDl.close();
                isYoutubeDl.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if ( dlAudio ) {
                isffmpeg = this.getClass().getClassLoader().getResourceAsStream("lib/ffmpeg");
                try {
                    fosffmpeg = new FileOutputStream(TEMP_FOLDER + "ffmpeg" + videoNumber);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    while ((c = isffmpeg.read()) != -1)
                        fosffmpeg.write(c);
                    fosffmpeg.close();
                    isffmpeg.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            download();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
