package com.example.smartchatters.View;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.smartchatters.R;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView view;
    private MediaController controller;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        view = (VideoView) findViewById(R.id.videoView);
        controller = new MediaController(this);
        path=(String)getIntent().getSerializableExtra("videoPath");
        view.setVideoPath(path);

        controller.setAnchorView(view);
        view.setMediaController(controller);
        view.start();
    }
}