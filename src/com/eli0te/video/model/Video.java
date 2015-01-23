package com.eli0te.video.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

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

    public Video(HashMap<String, String> videoInfo){
        this.videoTitle = new SimpleStringProperty(videoInfo.get("title"));
        this.videoDescription = new SimpleStringProperty(videoInfo.get("description"));
        this.videoDuration = new SimpleStringProperty(videoInfo.get("duration"));
        this.videoUploader = new SimpleStringProperty(videoInfo.get("uploader"));
        this.videoThumbnail = new SimpleStringProperty(videoInfo.get("thumbnail"));
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
}
