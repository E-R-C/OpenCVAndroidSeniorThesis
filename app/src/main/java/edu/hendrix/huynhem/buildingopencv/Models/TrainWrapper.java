package edu.hendrix.huynhem.buildingopencv.Models;

import android.os.AsyncTask;
import android.util.Log;


import java.util.List;

/**
 *
 */

public class TrainWrapper extends AsyncTask<ListLabelTuple, Integer, Void> {
    BaseModelInterface model;
    TrainModelAsyncInterface mListener;
    private final static String LOG_TAG = "TRAINWRAPPER";

    public TrainWrapper(BaseModelInterface model){
        this.model = model;
    }




    private void trainAll(List<String> strings, String label) {
        for (int i = 0; i < strings.size(); i++){
            String s = strings.get(i);
            model.incrementalTrain(s, label);
            publishProgress(i + 1, strings.size());
        }
    }


    @Override
    protected Void doInBackground(ListLabelTuple... listLabelTuples) {
        for (ListLabelTuple t : listLabelTuples){
            trainAll(t.getFileNames(), t.getLabel());
        };
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (mListener != null){
            mListener.publishTrainProgress(values);
        } else {
            Log.d(LOG_TAG, "No listener set! so No progress was pusblished!");
        }
    }



    public void setListener(TrainModelAsyncInterface listener){
        mListener = listener;
    }

}
