package edu.hendrix.huynhem.buildingopencv.Models;

import android.os.AsyncTask;

import java.util.ArrayList;

import edu.hendrix.huynhem.buildingopencv.Util.AsyncResponse;

/**
 *
 */

public class KnnClassifyWrapper extends AsyncTask<String, Integer, ArrayList<String>> {
    KnnTrainModel model;
    public AsyncResponse delegate = null;
    public KnnClassifyWrapper(KnnTrainModel model){
        this.model = model;
    }



    @Override
    protected ArrayList<String> doInBackground(String... files) {
        ArrayList<String> result = new ArrayList<>();
        for(int i = 0; i < files.length; i++){
            result.add(model.classify(files[i]));
            publishProgress(i,files.length);
        }
        return result;
    }

    public void setListener(AsyncResponse delegate){
        this.delegate = delegate;
    }
    @Override
    protected void onProgressUpdate(Integer... values) {
        if (delegate != null){
            delegate.publishProgress(values);
        }
    }

    @Override
    protected void onPostExecute(ArrayList<String> labels) {
        if (delegate != null){
            delegate.processFinish(labels);
        }
    }

}
