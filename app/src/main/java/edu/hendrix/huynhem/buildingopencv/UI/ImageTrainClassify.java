package edu.hendrix.huynhem.buildingopencv.UI;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import edu.hendrix.huynhem.buildingopencv.Models.BSOC.BSOCModel;
import edu.hendrix.huynhem.buildingopencv.Models.BSOCWholeImage.BSOCModelWholeImage;
import edu.hendrix.huynhem.buildingopencv.Models.BaseModelInterface;
import edu.hendrix.huynhem.buildingopencv.Models.ClassifyWrapper;
import edu.hendrix.huynhem.buildingopencv.Models.KnnTrainModel;
import edu.hendrix.huynhem.buildingopencv.Models.KnnWholeImageModel;
import edu.hendrix.huynhem.buildingopencv.Models.ListLabelTuple;
import edu.hendrix.huynhem.buildingopencv.Models.TrainModelAsyncInterface;
import edu.hendrix.huynhem.buildingopencv.Models.TrainWrapper;
import edu.hendrix.huynhem.buildingopencv.R;
import edu.hendrix.huynhem.buildingopencv.Util.GalleryUtil;

public class ImageTrainClassify extends Activity implements ClassifyWrapper.ClassifyWrapperInterface, TrainModelAsyncInterface{
    public static final String IMAGE_NAME_ARRAY_KEY = "IMAGE_ARRAY";
    private static final String LOG_TAG = "IMAGETRAINACTIVITY";
    private static final int REQUEST_IMAGE_FOR_CLASSIFY_FROM_GAL = 1234;
    private static final int REQUEST_IMAGE_FROM_GAL = 1337;
    private ArrayList<String> fileNames;
    private BaseModelInterface model;
    private ProgressBar progressBar;
    private Button trainButton, classifyFromGalButton, addMoreImagesButton, runTestButton;
    private ArrayList<ListLabelTuple> trainDatalist, testDataList;
    private TupleListAdapter trainListAdapter, testListAdapter;
    private TextView trainTextView, outputTextView;
    final HashMap<String, BaseModelInterface> modelMap = new HashMap<>();






    private void fillModelMap(HashMap<String, BaseModelInterface> map){
        for(String s: this.getResources().getStringArray(R.array.ModelTypes)){
            String[] split = s.split(" ");
            int num = Integer.parseInt(split[1]);
            switch (split[0]){
                case "KNN":
                    map.put(s, new KnnTrainModel(num));
                    break;
                case "BSOC":
                    map.put(s, new BSOCModel(num));
                    break;
                case "KNNWI":
                    map.put(s, new KnnWholeImageModel(num));
                    break;
                case "BSOCWI":
                    map.put(s, new BSOCModelWholeImage(num));
                    break;
                default:
                    Log.e(LOG_TAG, "INVALID MODEL");
                    break;
            }

        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_train_classify);

        // This allows us to refer to our current activity from inside the onclicklistener
        final ImageTrainClassify activtyReference = this;

        // Grab the selected files
        Bundle args = getIntent().getExtras();
        assert args != null;
        fileNames = args.getStringArrayList(IMAGE_NAME_ARRAY_KEY);

        // Set the text view
        outputTextView = findViewById(R.id.outputTextView);
        trainTextView = findViewById(R.id.TrainedStatusText);
        updateNumImages();
        // This initializes the spinner
        final Spinner modelSpinner = findViewById(R.id.modelSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.ModelTypes,android.R.layout.simple_spinner_item);
        modelSpinner.setAdapter(adapter);

        // Setup the buttons
        classifyFromGalButton = findViewById(R.id.ClassifyGalButton);
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
        addMoreImagesButton = findViewById(R.id.AddMoreImagesButton);
        addMoreImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(i, REQUEST_IMAGE_FROM_GAL);
            }
        });
        runTestButton = findViewById(R.id.runTest);
        runTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClassifyWrapper model2 = new ClassifyWrapper(model);
                model2.setListener(activtyReference);
                model2.execute(toListOfFiles(testDataList));
            }
        });
        trainButton = findViewById(R.id.TrainButton);
        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model = switchModels(modelSpinner.getSelectedItem().toString());
                train(model, trainDatalist);
                classifyFromGalButton.setEnabled(true);
            }
        });
        modelSpinner.setOnItemSelectedListener(new modelSpinnerItemSelectedListener(trainButton));

        progressBar = findViewById(R.id.ClassificationBar);

        // Initialize Empty lists for our ListViews
        trainDatalist = new ArrayList<>();
        testDataList = new ArrayList<>();

        // Initialize our ListViews
        ListView trainData = findViewById(R.id.train_data_list);
        trainListAdapter = new TupleListAdapter(this, R.layout.tuple_list_row, trainDatalist);
        trainData.setAdapter(trainListAdapter);

        ListView testData = findViewById(R.id.test_data_list);
        testListAdapter = new TupleListAdapter(this, R.layout.tuple_list_row, testDataList);
        testData.setAdapter(testListAdapter);

        // Generate the alert for the first time instance:
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.label);
        builder.setMessage("Give the selected image(s) a label and tell me where to put it");
        final EditText edittext = new EditText(getApplicationContext());
        builder.setView(edittext);
        // Todo Fix the fact that this code is basically a copy of the alert in response
        builder.setPositiveButton("Train", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ListLabelTuple llt = new ListLabelTuple(fileNames, edittext.getText().toString());
                trainDatalist.add(llt);
                trainListAdapter.notifyDataSetChanged();
            }
        });
        builder.setNeutralButton("Test", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ListLabelTuple llt = new ListLabelTuple(fileNames, edittext.getText().toString());
                testDataList.add(llt);
                testListAdapter.notifyDataSetChanged();
            }
        });
        builder.show();

    }

    private BaseModelInterface switchModels(String modelName){
        if (modelMap.size() < 1) {
            fillModelMap(modelMap);
        }
        if(model != null){
            model.dealloc();
        }
        return modelMap.get(modelName).constructNew();


    }
    public void train(BaseModelInterface m, ArrayList<ListLabelTuple> dataList){
        TrainWrapper tw = new TrainWrapper(m);
        tw.setListener(this);
        tw.execute(dataList.toArray(new ListLabelTuple[dataList.size()]));
        trainButton.setEnabled(false);
        trainButton.setText(getText(R.string.AlreadyTrained));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean ok = resultCode == Activity.RESULT_OK;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.label);
        builder.setMessage("Give the selected image(s) a label and tell me where to put it");
        final EditText edittext = new EditText(getApplicationContext());
        builder.setView(edittext);
        // Todo check for blank edittext input
        builder.setPositiveButton("Train", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ListLabelTuple llt = new ListLabelTuple(fileNames, edittext.getText().toString());
                trainDatalist.add(llt);
                trainListAdapter.notifyDataSetChanged();
            }
        });
        builder.setNeutralButton("Test", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ListLabelTuple llt = new ListLabelTuple(fileNames, edittext.getText().toString());
                testDataList.add(llt);
                testListAdapter.notifyDataSetChanged();
                runTestButton.setEnabled(true);
            }
        });



        if (requestCode == REQUEST_IMAGE_FOR_CLASSIFY_FROM_GAL && ok){
            ArrayList<String> filenames = GalleryUtil.extractArrayList(this, data);
            ClassifyWrapper model2 = new ClassifyWrapper(model);
            model2.setListener(this);
            model2.execute(GalleryUtil.arraylistToStringarray(filenames));

        }
        else if (requestCode == REQUEST_IMAGE_FROM_GAL && ok){
            fileNames = GalleryUtil.extractArrayList(this, data);
            trainButton.setText(R.string.train_model);
            trainButton.setEnabled(true);
            trainListAdapter.notifyDataSetChanged();
            updateNumImages();
            builder.show();
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
    }

    @Override
    public void processClassifyFinish(ArrayList<String> labels) {
        String[] actualLabels = toListOfLabels(testDataList);
        int correct = 0;
        for(int i = 0; i < labels.size(); i++){
            if (labels.get(i).equals(actualLabels[i])){
                correct++;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(correct);
        sb.append("/");
        sb.append(labels.size());
        sb.append(" ");
        sb.append(((float) correct) / labels.size());
        sb.append("%\n");
        for (String s : labels){
            sb.append(s);
            sb.append("\n");
        }
        outputTextView.setText(sb.toString());
    }

    @Override
    public void publishTrainProgress(Integer... ints) {
        trainTextView.setText(String.format(getText(R.string.TrainingString).toString(), ints));

    }


    private static String[] toListOfFiles(ArrayList<ListLabelTuple> labels){
        ArrayList<String> result = new ArrayList<>();
        int count = 0;
        for(ListLabelTuple l: labels){
            result.addAll(l.getFileNames());
            count += l.getFileNames().size();
        }
        return result.toArray(new String[count]);
    }

    private static String[] toListOfLabels(ArrayList<ListLabelTuple> labels){
        ArrayList<String> result = new ArrayList<>();
        int count = 0;
        for(ListLabelTuple l: labels){
            for(String filename: l.getFileNames()){
                result.add(l.getLabel());
            }
            count += l.getFileNames().size();
        }
        return result.toArray(new String[count]);
    }


    // Spinner on click listener
    public class modelSpinnerItemSelectedListener implements AdapterView.OnItemSelectedListener{
        int currentSelected = 0;
        Button b;

        public modelSpinnerItemSelectedListener(Button trainButton){
            b = trainButton;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int index, long id) {
            if (index != currentSelected){
                currentSelected = index;
                b.setEnabled(true);
                b.setText(R.string.retrain_model);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }
}
