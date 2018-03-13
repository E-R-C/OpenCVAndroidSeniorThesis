package edu.hendrix.huynhem.buildingopencv.Models;

import android.os.AsyncTask;

import java.util.ArrayList;

/**
 *
 */

public class ClassifyWrapper extends AsyncTask<String, Integer, ArrayList<String>>  {
    BaseModelInterface model;
    public ClassifyWrapper(KnnTrainModel model){
        this.model = model;
    }
    ClassifyWrapperInterface mListener;

    public ClassifyWrapper(BaseModelInterface model){
        this.model = model;
    }
    @Override
    protected ArrayList<String> doInBackground(String... files) {
        ArrayList<String> result = new ArrayList<>();
        for(int i = 0; i < files.length; i++){
            result.add(model.classify(files[i]));
            publishProgress(i + 1,files.length);
        }
        return result;
    }

    public void setListener(ClassifyWrapperInterface delegate){
        this.mListener = delegate;
    }
    @Override
    protected void onProgressUpdate(Integer... values) {
        if (mListener != null){
            mListener.publishClassifyProgress(values);
        }
    }

    @Override
    protected void onPostExecute(ArrayList<String> labels) {
        if (mListener != null){
            mListener.processClassifyFinish(labels);
        }
    }

    public interface ClassifyWrapperInterface {
        void publishClassifyProgress(Integer... nums);
        void processClassifyFinish(ArrayList<String> output);
    }
}
