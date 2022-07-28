package com.example.smartchatters.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

public class Singleton {
    private static Singleton instance = new Singleton();

    public static Singleton getInstance() {
        return instance;
    }

    private Usernode user;

    private ExecutorService executorService;

    private HashMap<String,Integer> needRestart = new HashMap<>();


    public HashMap<String,Integer> getNeedRestart() {
        return needRestart;
    }

    public void setNeedRestart(String topicName, Integer counter) {
        needRestart.put(topicName,counter);
    }

    public int getCounter(String topicName)
    {
        return needRestart.get(topicName);
    }


    public Usernode getUser() {
        return user;
    }

    public void setUser(Usernode user) {
        this.user = user;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
}