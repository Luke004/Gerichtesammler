package com.example.lhilf.leistungensammler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.result.PendingResult;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.view.CameraView;
import kotlin.Unit;

public class CameraActivity extends AppCompatActivity {

    private String m_current_photo_path;
    private Fotoapparat fotoapparat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        CameraView cameraView = findViewById(R.id.camera_view);
        fotoapparat = new Fotoapparat(this, cameraView);

        findViewById(R.id.take_photo_btn).setOnClickListener(v -> {
            PhotoResult photoResult = fotoapparat.takePicture();

            File photoFile = null;
            try {
                photoFile = Helper.createImageFile(this);
                m_current_photo_path = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            PendingResult<Unit> pendingResult = photoResult.saveToFile(photoFile);

            pendingResult.whenDone(unit -> {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("photo_path", m_current_photo_path);
                setResult(Activity.RESULT_OK, returnIntent);

                finish();
            });
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        fotoapparat.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        fotoapparat.stop();
    }

}
