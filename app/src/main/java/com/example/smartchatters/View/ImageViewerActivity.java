package com.example.smartchatters.View;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.smartchatters.R;

public class ImageViewerActivity extends AppCompatActivity {

    private ImageView view;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        view = (ImageView) findViewById(R.id.imageViewDisplayId);
        path=(String)getIntent().getSerializableExtra("imagePath");

        final Bitmap b = BitmapFactory.decodeFile(path);

        view.setImageBitmap(b);
    }
}