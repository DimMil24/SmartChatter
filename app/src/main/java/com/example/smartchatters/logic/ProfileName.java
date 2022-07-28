package com.example.smartchatters.logic;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ProfileName implements Serializable {

    private static final long serialVersionUID = -1L;
    private String username;
    private String firstname;
    private String lastname;
    private ArrayList<String> subscribedTopics;
    private HashMap<String, ArrayList<MultimediaFile>> topicsFilesIds;

    public ProfileName(String username, String firstname, String lastname){

        this.username=username;
        this.firstname=firstname;
        this.lastname=lastname;
        subscribedTopics = new ArrayList<String>();
        topicsFilesIds = new HashMap<>();
    }


    public String getUsername(){
        return username;
    }

    public String getFirstname(){
        return firstname;
    }

    public String getLastname(){
        return lastname;
    }

    public String toString() {
        return username;
    }


    public void subscribeTo(String topicName) {
        subscribedTopics.add(topicName);
        topicsFilesIds.put(topicName, new ArrayList<>());
    }

    public void addFile(String topicName, MultimediaFile file){
        topicsFilesIds.get(topicName).add(file);
    }

    public Boolean subscribedTo(String topicName) {
        for (String tN: subscribedTopics){
            if (topicName.equals(tN)){
                return true;
            }
        }
        return false;
    }

    public ArrayList<MultimediaFile> getMultimediaFiles(String topicName) {
        return topicsFilesIds.get(topicName);
    }

}



