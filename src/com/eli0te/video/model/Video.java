package com.eli0te.video.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.util.HashMap;

/**
 * Created by ~eLi0tE~ on 23/01/2015.
 */
public class Video {

    private final StringProperty videoTitle;
    private final StringProperty videoDescription;
    private final StringProperty videoDuration;
    private final StringProperty videoUploader;
    private final StringProperty videoThumbnail;
    private final String videoUrl;
    private final String playlistTitle;

    private final BooleanProperty toDownload;


    public Video(HashMap<String, String> videoInfo){
        this.videoTitle = new SimpleStringProperty(videoInfo.get("title"));
        this.videoDescription = new SimpleStringProperty(videoInfo.get("description"));
        this.videoDuration = new SimpleStringProperty(videoInfo.get("duration"));
        this.videoUploader = new SimpleStringProperty(videoInfo.get("uploader"));
        this.videoThumbnail = new SimpleStringProperty(videoInfo.get("thumbnail"));
        this.toDownload = new SimpleBooleanProperty(true);
        this.videoUrl = videoInfo.get("videoUrl");
        this.playlistTitle = videoInfo.get("playlistTitle");
    }

    public String getVideoDuration() {
        return videoDuration.get();
    }

    public StringProperty videoDurationProperty() {
        return videoDuration;
    }

    public String getVideoTitle() {
        return videoTitle.get();
    }

    public StringProperty videoTitleProperty() {
        return videoTitle;
    }

    public String getVideoDescription() {
        return videoDescription.get();
    }

    public StringProperty videoDescriptionProperty() {
        return videoDescription;
    }

    public String getVideoUploader() {
        return videoUploader.get();
    }

    public StringProperty videoUploaderProperty() {
        return videoUploader;
    }

    public String getVideoThumbnail() {
        return videoThumbnail.get();
    }

    public StringProperty videoThumbnailProperty() {
        return videoThumbnail;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public boolean getToDownload() {
        return toDownload.get();
    }

    public BooleanProperty toDownloadProperty() {
        return toDownload;
    }

    public void setToDownload(boolean toDownload) {
        this.toDownload.set(toDownload);
    }
}
