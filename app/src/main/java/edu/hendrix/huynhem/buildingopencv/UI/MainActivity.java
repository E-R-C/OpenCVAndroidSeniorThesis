package edu.hendrix.huynhem.buildingopencv.UI;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;


import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import edu.hendrix.huynhem.buildingopencv.R;
import edu.hendrix.huynhem.buildingopencv.Util.FilePather;

public class MainActivity extends Activity {

    private final static String LOG_TAG = "MainActivity";
    // Used to load the 'native-lib' library on application startup.

    private final static int REQUEST_IMAGE_FROM_GAL = 123;
    private final static int REQUEST_CAMERA_PERMISSION = 1234;
    private final static int REQUEST_IMAGE_CAPTURE = 12345;
    private final static String ALBUM_NAME = "BuildingOPENCV";

    static {
        System.loadLibrary("native-lib");
//        System.loadLibrary("opencv_java3");

//        Use this to load during dev;
        OpenCVLoader.initDebug();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button chooseFromGallery = findViewById(R.id.PicFromGalButton);
        requestPermissions();
        chooseFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    requestPermissions();
                } else {
                    Intent i = new Intent(Intent.ACTION_PICK);
                    i.setType("image/*");
                    i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    i.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(i, REQUEST_IMAGE_FROM_GAL);
                }

            }
        });
        Button captureNewImageButton = findViewById(R.id.PicFromCam);
        captureNewImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
            }
        });


        // Example of a call to a native method
//        TextView tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
    }

    private void requestPermissions(){
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
    }

    private void requestPermissions(String[] permissions){
        this.requestPermissions(permissions, REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != grantResults[0]) {
                Log.d(LOG_TAG, "I need permission! I didn't get it");
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasPermission(String manifestDescription){
        return this.checkSelfPermission(manifestDescription) == PackageManager.PERMISSION_GRANTED;
    }
    private void captureImage(){
        if (!hasPermission(Manifest.permission.CAMERA) || !hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.d(LOG_TAG, "Asking for permission!");
            requestPermissions();
            return;
        }
        Log.d(LOG_TAG, "Starting intent To Capture Image");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File outputFile;
        try {
            outputFile = createNextFile();

            Uri photoLocationUri = FileProvider.getUriForFile(this,"edu.hendrix.huynhem",
                    outputFile
            );

            Log.d(LOG_TAG, "LOCATION OF FILE: " + photoLocationUri.toString());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoLocationUri);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException e) {
            Log.d(LOG_TAG, "Failed to create file, This is why your program is failing right now");
        }
    }

    private File createNextFile() throws IOException {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + ALBUM_NAME);
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(LOG_TAG, "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prefix = "IMG_" + timeStamp;
        File storageDir = mediaStorageDir;
        File image = File.createTempFile(
                prefix,
                ".jpg",
                storageDir
        );
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean ok = resultCode == Activity.RESULT_OK;
        if ((requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_IMAGE_FROM_GAL )&& ok){
            Intent intent = new Intent(this, ImageTrainClassify.class);
            ArrayList<String> filenames = new ArrayList<>();
            if (data.getClipData() != null) {
                int numberOfImages = data.getClipData().getItemCount();
                for (int i = 0; i < numberOfImages; i++) {
                    Log.d(LOG_TAG, data.getClipData().getItemAt(i).getUri().toString());
                    filenames.add(FilePather.getFilePath(this, data.getClipData().getItemAt(i).getUri()));
                }

            } else {
                Log.d(LOG_TAG, "No data selected in Clip Data");
                final Uri imageUri = data.getData();
                filenames.add(FilePather.getFilePath(this,imageUri));
            }
            intent.putExtra(ImageTrainClassify.IMAGE_NAME_ARRAY_KEY,filenames);
            startActivity(intent);
        }
    }

}
