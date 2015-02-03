package com.eli0te.video;

import com.eli0te.video.view.VideoOverviewController;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by eLi0tE on 16/01/15.
 * No more used
 */
public class Helper {

    private static String OS = System.getProperty("os.name").toLowerCase();

    String cmd, cmd2;


    public Helper() {
        // Uses of the right library depending on OS
        if ( isWindowsOS() ) {
            cmd = System.getProperty("user.dir") + "\\lib\\youtube-dl.exe";
        } else {
            cmd = System.getProperty("user.dir") + "/lib/youtube-dl";
        }
    }

    public void getVideo(String videoURL, String outputPath) throws Exception { }

    public ArrayList<HashMap<String, String>> getInformation(String url) throws Exception{

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

        ArrayList<HashMap<String, String>> infoMapList = new ArrayList<>();


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

            // Si une seule vidéo est retournée par le lien, pas de playlistTitle
            try {
                infoMap.put("playlistTitle", line.getString("playlist_title"));
            } catch (JSONException e) {
                infoMap.put("playlistTitle", "Not in a playlist");
            }


            infoMapList.add(i, infoMap);
            i++;
        }
        System.out.println(i);
        return infoMapList;
    }

    public void getAudio(String videoURL, VideoOverviewController controller, String outputPath) throws Exception {
        Process[] p = new Process[2];

        p[0] = new ProcessBuilder(cmd, "--get-filename", videoURL).start();

        BufferedReader in = new BufferedReader( new InputStreamReader(p[0].getInputStream()) );


        // Récupérer le tableau créé avec le bouton checkUrl


        //    ArrayList<HashMap<String,String>> infoMapList = getInformation(videoURL);

      /*
        for (HashMap<String, String> hashMap: infoMapList){
            al.updateEventList("Nom de la vidéo : " + hashMap.get("title"));
            al.updateEventList("Description : " + hashMap.get("description"));
        }
*/
        // Dynamic construction of the outputPath depending on operating system
        cmd2 = controller.getDownloadPath();
        if ( isWindowsOS() ){
            cmd2 += "\\";
        } else {
            cmd2 += "/";
        }
        cmd2 += "%(title)s.%(ext)s";

        p[1] = new ProcessBuilder(cmd,
                "-i",
                videoURL,
                "-x",
                "--audio-format",
                "mp3",
                "--audio-quality",
                "0",
                "-o",
                cmd2
        ).start();



        //youtube-dl.exe https://www.youtube.com/watch?v=2F6d6crjRyU -x --audio-format "mp3" --audio-quality 0 -o C:\Users\Marius\Music\Youtube\%(title)s.%(ext)s


        in = new BufferedReader( new InputStreamReader(p[1].getInputStream()) );
        String cmdOutput;
        String s;

        while ( (cmdOutput = in.readLine()) != null ) {

            System.out.println(cmdOutput);
            if ( cmdOutput.contains("[download] ") && cmdOutput.contains("%")  ) {
                s = cmdOutput.substring("[download] ".length(), cmdOutput.indexOf('%'));
                if ( s.contains(".") ) // Exclusion du dernier 100% déjà en double
                    controller.updateProgress(Double.parseDouble(s));
            }
        }
        in.close();
    }

    private static String RemoveIllegalPathCharacters(String path)
    {
        return path.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    private static String RemoveIllegalPathCharactersForRename(String path)
    {
        return path.replaceAll("[^a-zA-Z0-9.-]", " "); // Comprends pas c'est la même qu'au dessus j'avais fais celle la
        //pour le renomage de fichier // tu preans une classends le fichier à la fain et tu le rename ? Car c'est youtube=dl qui nome lesfichiers
        // bah ouais c'est dégeulasse sinon attebdje vais rajouter des trucs d
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

    private static boolean isWindowsOS(){
        if ( OS.indexOf("win") >= 0 )
            return true;
        return false;
    }
}
