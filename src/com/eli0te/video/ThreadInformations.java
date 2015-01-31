package com.eli0te.video;

import com.eli0te.video.view.VideoOverviewController;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by eLi0tE on 16/01/15.
 * Utilisation du programme youtube-dl en python
 */
public class ThreadInformations implements Runnable {

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static String TEMP_FOLDER = "";


    ArrayList<HashMap<String, String>> infoMapList = new ArrayList<>();

    public ArrayList<HashMap<String, String>> getInfoMapList() {
        return infoMapList;
    }

    public void setInfoMapList(ArrayList<HashMap<String, String>> infoMapList) {
        this.infoMapList = infoMapList;
    }


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


    public ThreadInformations(String url, int threadNumber, ArrayList<HashMap<String, String>> infoMapList ) {
        setThreadNumber(threadNumber);
        setUrl(url);
        setInfoMapList(infoMapList);
    }

    public ArrayList<HashMap<String, String>> getInformation(String url) throws Exception{

        String cmd = "";

        if ( isWindowsOS() ) {
            TEMP_FOLDER = System.getProperty("java.io.tmpdir")+"musicExtractorTemp\\";
            cmd += TEMP_FOLDER +"youtube-dl.exe";
        } else {
            TEMP_FOLDER = System.getProperty("java.io.tmpdir")+"musicExtractorTemp/";
            cmd += TEMP_FOLDER +"youtube-dl";
        }

        Process[] p = new Process[1];

        try {
            p[0] = new ProcessBuilder(cmd, "-i", "-j", url).start();
        } catch (IOException e){
            p[0] = new ProcessBuilder("chmod", "a+x",cmd).start();
            p[0] = new ProcessBuilder(cmd,"-i", "-j", url).start();
        }


        InputStreamReader is = new InputStreamReader(p[0].getInputStream());

        BufferedReader in = new BufferedReader( is );
        String cmdOutput;

        int i = 0;
        HashMap<String, String> infoMap;
        String duration;

        //Chaque ligne retourné est égale aux infos d'une vidéos (si playlist, plusieurs lignes)
        while ( (cmdOutput = in.readLine() ) != null ) {
            // Traiter cmdOutput (Json)
            //in.
            JSONObject line = new JSONObject(cmdOutput);

            infoMap = new HashMap<>();

            infoMap.put("title", line.getString("title"));
            infoMap.put("description", line.getString("description"));
            infoMap.put("thumbnail", line.getString("thumbnail"));
            infoMap.put("duration", getFormatDuration(line.getInt("duration")));
            infoMap.put("uploader", line.getString("uploader"));
            infoMap.put("videoUrl", line.getString("webpage_url"));
            infoMap.put("playlistTitle", line.getString("playlist_title"));


            infoMapList.add(i, infoMap);
            i++;
        }
        System.out.println(i);
        return infoMapList;
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

    private static boolean isWindowsOS(){
        if ( OS.indexOf("win") >= 0 )
            return true;
        return false;
    }

    @Override
    public void run() {

        File youtubeDl = new File("lib\\youtube-dl.exe");
        File tempFolder = new File(TEMP_FOLDER);
        tempFolder.mkdirs();
        File destYoutubeDl = new File(TEMP_FOLDER+"youtube-dl("+String.valueOf(getThreadNumber())+").exe");
        try {
            System.out.println("copie de youtubeDl ("+getThreadNumber()+")");
            Files.copy(youtubeDl.toPath(), destYoutubeDl.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO Appeller getInformations

        
        System.out.println("Suppression des fichiers (" + getThreadNumber() + ")");
        destYoutubeDl.delete();
        tempFolder.delete();

    }
}
