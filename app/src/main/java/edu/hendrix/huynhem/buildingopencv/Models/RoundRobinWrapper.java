package edu.hendrix.huynhem.buildingopencv.Models;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */

public class RoundRobinWrapper extends AsyncTask<ListLabelTuple, Integer, Double> {
    BaseModelInterface model;
    RoundRobinInterface mListner;
    public RoundRobinWrapper(BaseModelInterface model){
        this.model = model;
    }
    public void setListener(RoundRobinInterface rri){
        mListner = rri;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if(mListner != null){
            mListner.RobinProgress(values);
        }
    }

    @Override
    protected Double doInBackground(ListLabelTuple... listLabelTuples) {
        List<String> files = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for(ListLabelTuple d: listLabelTuples){
            int size = d.getFileNames().size();
            files.addAll(d.getFileNames());
            for(int i = 0; i < size; i++){
                labels.add(d.getLabel());
            }
        }

        String chosenLabel;
        String chosenFile;
        int correct = 0;
        int total = labels.size();
        for(int i = 0; i < labels.size(); i++){
            chosenFile = files.remove(0);
            chosenLabel = labels.remove(0);
            BaseModelInterface model2 = model.constructNew();
            for(int j = 0; j < labels.size(); j++){
                model2.incrementalTrain(files.get(j), labels.get(j));
            }
            String result = model2.classify(chosenFile);
            if(result.equals(chosenLabel)){
                correct += 1;
            }
            files.add(chosenFile);
            labels.add(chosenLabel);
            publishProgress(i + 1, total);
        }
        return correct/(double)total;
    }

    @Override
    protected void onPostExecute(Double aDouble) {
        if(mListner != null){
            mListner.RobinResults(aDouble);
        }
    }

    public interface RoundRobinInterface{
        void RobinResults(double d);
        void RobinProgress(Integer... ints);
    }
}
