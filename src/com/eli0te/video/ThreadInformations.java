package com.eli0te.video;

import com.eli0te.video.model.Video;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;

/**
 * Created by eLi0tE on 16/01/15.
 * Utilisation du programme youtube-dl en python
 */
public class ThreadInformations implements Runnable {

    private static String OS = System.getProperty("os.name").toLowerCase();



    private MainApp mainApp;

    //url
    private String url;
    public void setUrl(String url) {
        this.url = url;
    }


    public ThreadInformations(String url, MainApp mainApp) {
        this.mainApp = mainApp;
        setUrl(url);
    }

    public void getInformation(String url) throws Exception {

        Process p;

        p = new ProcessBuilder(mainApp.getYoutubeDlPathOut(), "-i", "-j", url).start();


        InputStreamReader is = new InputStreamReader(p.getInputStream());

        BufferedReader in = new BufferedReader( is );
        String cmdOutput;

        int videoNumber = 0;
        HashMap<String, String> infoMap;

        mainApp.clearVideoToList();

        //Chaque ligne retourné est égale aux infos d'une vidéos (si playlist, plusieurs lignes)
        while ( (cmdOutput = in.readLine() ) != null ) {

            JSONObject line = new JSONObject(cmdOutput);

            infoMap = new HashMap<>();

            infoMap.put("title", line.getString("title"));
            infoMap.put("description", line.getString("description"));
            infoMap.put("thumbnail", line.getString("thumbnail"));
            infoMap.put("duration", getFormatDuration(line.getInt("duration")));
            infoMap.put("uploader", line.getString("uploader"));
            infoMap.put("videoUrl", line.getString("webpage_url"));
            infoMap.put("videoNumber", String.valueOf(videoNumber));
            try {
                infoMap.put("playlistTitle", line.getString("playlist_title"));
            } catch (Exception e) {
                infoMap.put("playlistTitle", "Not in a playlist");
            }

            mainApp.addVideoToList(new Video(infoMap));
            System.out.println(videoNumber + " : Ajout de la vidéo : " + infoMap.get("title") + " à la liste");
            videoNumber++;
        }
        return;
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
    private void printProcessOutput(Process p){
        BufferedReader in = new BufferedReader( new InputStreamReader(p.getInputStream()));
        String cmdOutput;
        try {
            while ( (cmdOutput = in.readLine()) != null ) { System.out.println(cmdOutput); }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            getInformation(url);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
