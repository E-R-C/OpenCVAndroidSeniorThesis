package edu.hendrix.huynhem.buildingopencv.UI;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.hendrix.huynhem.buildingopencv.Models.BSOC.BSOCModel;
import edu.hendrix.huynhem.buildingopencv.Models.BaseModelInterface;
import edu.hendrix.huynhem.buildingopencv.Models.ClassifyWrapper;
import edu.hendrix.huynhem.buildingopencv.Models.KnnTrainModel;
import edu.hendrix.huynhem.buildingopencv.Models.ListLabelTuple;
import edu.hendrix.huynhem.buildingopencv.Models.RoundRobinWrapper;
import edu.hendrix.huynhem.buildingopencv.Models.TrainModelAsyncInterface;
import edu.hendrix.huynhem.buildingopencv.Models.TrainWrapper;
import edu.hendrix.huynhem.buildingopencv.R;
import edu.hendrix.huynhem.buildingopencv.Util.GalleryUtil;

public class ImageTrainClassify extends Activity implements ClassifyWrapper.ClassifyWrapperInterface, TrainModelAsyncInterface, RoundRobinWrapper.RoundRobinInterface{
    public static final String IMAGE_NAME_ARRAY_KEY = "IMAGE_ARRAY";
    private static final String LOG_TAG = "IMAGETRAINACTIVITY";
    private static final int REQUEST_IMAGE_FOR_CLASSIFY_FROM_GAL = 1234;
    private static final int REQUEST_IMAGE_FOR_TRAIN_FROM_GAL = 1337;
    private ArrayList<String> fileNames;
    private BaseModelInterface model;
    private TextView trainTextView;
    private ProgressBar progressBar;
    private Button trainButton;
    private ArrayList<ListLabelTuple> dataList;
    private TupleListAdapter tla;
    private TextView outputTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final ImageTrainClassify activtyReference = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_train_classify);
        Bundle args = getIntent().getExtras();
        fileNames = args.getStringArrayList(IMAGE_NAME_ARRAY_KEY);
        final TextView labelTextView = findViewById(R.id.LabelText);
        updateNumImages();
        trainButton = findViewById(R.id.TrainButton);
        // TODO: change the model based on the selection from the spinner;
        model = new BSOCModel();
        dataList = new ArrayList<>();
        outputTextView = findViewById(R.id.outputTextView);

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!labelTextView.getText().toString().equals("")){
                    model.dealloc();
                    // change the following line to change the model type
                    model = model.constructNew();
                    TrainWrapper tw = new TrainWrapper(model);
                    tw.setListener(activtyReference);
                    ListLabelTuple llt = new ListLabelTuple(fileNames, labelTextView.getText().toString());
                    dataList.add(llt);
                    tw.execute(dataList.toArray(new ListLabelTuple[dataList.size()]));
                    trainButton.setEnabled(false);
                    trainButton.setText(getText(R.string.AlreadyTrained));
                    labelTextView.setText("");
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
                startActivityForResult(i, REQUEST_IMAGE_FOR_CLASSIFY_FROM_GAL);
            }
        });
        Button addMoreImages = findViewById(R.id.AddMoreImagesButton);
        addMoreImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(i, REQUEST_IMAGE_FOR_TRAIN_FROM_GAL);
            }
        });
        trainTextView = findViewById(R.id.TrainedStatusText);
        progressBar = findViewById(R.id.ClassificationBar);
        ListView dataHighlighted = findViewById(R.id.dataHighlighted);
        tla = new TupleListAdapter(this, R.layout.tuple_list_row,dataList);
        dataHighlighted.setAdapter(tla);
        Button roundRobinButton = findViewById(R.id.RoundRobinButton);
        roundRobinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RoundRobinWrapper rrw = new RoundRobinWrapper(model);
                rrw.setListener(activtyReference);
                rrw.execute(dataList.toArray(new ListLabelTuple[dataList.size()]));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean ok = resultCode == Activity.RESULT_OK;
        if (requestCode == REQUEST_IMAGE_FOR_CLASSIFY_FROM_GAL && ok){
            ArrayList<String> filenames = GalleryUtil.extractArrayList(this, data);
            ClassifyWrapper model2 = new ClassifyWrapper( model);
            model2.setListener(this);
            model2.execute(GalleryUtil.arraylistToStringarray(filenames));

        }
        else if (requestCode == REQUEST_IMAGE_FOR_TRAIN_FROM_GAL && ok){
            fileNames = GalleryUtil.extractArrayList(this, data);
            trainButton.setText(R.string.train_model);
            trainButton.setEnabled(true);
            tla.notifyDataSetChanged();
            updateNumImages();
        }
    }


    private void updateNumImages(){
        TextView numSelectedImagesTV = findViewById(R.id.NumSelectedImages);
        numSelectedImagesTV.setText(getString(R.string.number_of_selected_images) + " " + fileNames.size());
    }
    @Override
    public void publishClassifyProgress(Integer... nums) {
        progressBar.setMax(nums[1]);
        progressBar.setProgress(nums[0]);
        Log.d(LOG_TAG, "Logged Progress for Classification");
    }

    @Override
    public void processClassifyFinish(ArrayList<String> labels) {
        Log.d(LOG_TAG, "Classify Finished for Classification");
        StringBuilder sb = new StringBuilder();
        for (String s : labels){
            sb.append(s);
            sb.append("\n");
        }
        outputTextView.setText(sb.toString());
    }

    @Override
    public void publishTrainProgress(Integer... ints) {
        Log.d(LOG_TAG, String.format(getText(R.string.TrainingString).toString(), ints));
        trainTextView.setText(String.format(getText(R.string.TrainingString).toString(), ints));
        Log.d(LOG_TAG, "Logged Progress for Train");
    }


    @Override
    public void RobinResults(double d) {
        outputTextView.setText((d * 100) + "% Correct");
    }

    @Override
    public void RobinProgress(Integer... ints) {
        progressBar.setMax(ints[1]);
        progressBar.setProgress(ints[0]);
    }
}
