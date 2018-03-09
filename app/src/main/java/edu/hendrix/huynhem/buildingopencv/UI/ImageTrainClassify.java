package edu.hendrix.huynhem.buildingopencv.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.hendrix.huynhem.buildingopencv.Models.KnnClassifyWrapper;
import edu.hendrix.huynhem.buildingopencv.Models.KnnTrainModel;
import edu.hendrix.huynhem.buildingopencv.Models.TrainerInterface;
import edu.hendrix.huynhem.buildingopencv.R;
import edu.hendrix.huynhem.buildingopencv.Util.AsyncResponse;
import edu.hendrix.huynhem.buildingopencv.Util.FilePather;

public class ImageTrainClassify extends Activity implements AsyncResponse{
    public static final String IMAGE_NAME_ARRAY_KEY = "IMAGE_ARRAY";
    private static final int REQUEST_IMAGE_FROM_GAL = 1234;
    private ArrayList<String> fileNames;
    private TrainerInterface model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_train_classify);
        Bundle args = getIntent().getExtras();
        fileNames = args.getStringArrayList(IMAGE_NAME_ARRAY_KEY);
        final TextView labelTextView = findViewById(R.id.LabelText);
        Button trainButton = findViewById(R.id.TrainButton);
        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model = new KnnTrainModel();
                if (!labelTextView.getText().toString().equals("")){
                    model.trainAll(fileNames,labelTextView.getText().toString());
                    // TODO: once we introduce more models, we can't cast to knn anymore.
                    ((KnnTrainModel) model).execute(new String[1]);
                } else {
                    Toast.makeText(view.getContext(),"Give the image(s) a label!",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button classifyFromGalButton = findViewById(R.id.ClassifyGalButton);
        classifyFromGalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(i, REQUEST_IMAGE_FROM_GAL);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean ok = resultCode == Activity.RESULT_OK;
        if (requestCode == REQUEST_IMAGE_FROM_GAL && ok){
            if (data.getClipData() != null) {
                int numberOfImages = data.getClipData().getItemCount();
                String[] filenames = new String[numberOfImages];
                for (int i = 0; i < numberOfImages; i++) {
                    filenames[i] = FilePather.getFilePath(this.getApplicationContext(), data.getClipData().getItemAt(i).getUri());
                }
                // TODO Casting issue again
                KnnClassifyWrapper model2 = new KnnClassifyWrapper((KnnTrainModel) model);
                model2.setListener(this);
                model2.execute(filenames);
            }
        }
    }

    @Override
    public void publishProgress(Integer... nums) {
        // TODO: Update a notification or progress bar
    }

    @Override
    public void processFinish(ArrayList<String> labels) {
        //TODO: what are you going to do with the labels?
        TextView tv = findViewById(R.id.outputTextView);
        StringBuilder sb = new StringBuilder();
        for (String s : labels){
            sb.append(s);
            sb.append("\n");
        }
        tv.setText(sb.toString());
    }
}
