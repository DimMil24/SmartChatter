package com.example.smartchatters.logic;

import java.io.*;

public class MultimediaFile implements Serializable {

    private static final long serialVersionUID = -1L;

    private int chunkId;
    private String name;
    private ProfileName profileName;
    private String dateCreated;
    private String extension;
    private int fileId;

    long numberOfChunks;
    byte [] FileChunk;

    public MultimediaFile(String name, ProfileName profileName, String dateCreated,String extension,long numberOfChunks, int chunkSize,int chunkId) {
        this.name = name;
        this.profileName = profileName;
        this.dateCreated = dateCreated;
        this.extension=extension;
        this.numberOfChunks = numberOfChunks;
        this.chunkId=chunkId;
        FileChunk = new byte[chunkSize];
        fileId=0;
    }

    /* Returns the name of the current chunk file. */

    public String getName() {
        return name;
    }

    /* Returns the date of the creation of the file. */

    public String getDate() {
        return dateCreated;
    }

    /* Returns the file extension. */

    public String getExtension() {
        return extension;
    }

    /* Returns the profile name of the file chunk. */

    public ProfileName getProfileName() {
        return profileName;
    }

    /* Returns the chunk id. */

    public int getChunkId() {
        return chunkId;
    }

    /* Print function of the file chunk. */

    public String toString() {
        return name +"."+extension+" by " + profileName.getUsername();
    }

    public void setFileId(int fileId) {
        this.fileId=fileId;
    }

    public int getFileId() {
        return fileId;
    }
}