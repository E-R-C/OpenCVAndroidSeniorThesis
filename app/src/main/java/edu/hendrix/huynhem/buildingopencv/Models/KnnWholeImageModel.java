package edu.hendrix.huynhem.buildingopencv.Models;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import edu.hendrix.huynhem.buildingopencv.Util.Histogram;
import edu.hendrix.huynhem.buildingopencv.Util.Tuple;

import static org.opencv.core.Core.norm;

/**
 *
 */

public class KnnWholeImageModel implements BaseModelInterface {

    // Default create(int nfeatures = 500, float scaleFactor = 1.2f, int nlevels = 8, int edgeThreshold = 31, int firstLevel = 0, int WTA_K = 2, int scoreType = ORB::HARRIS_SCORE, int patchSize = 31, int fastThreshold = 20)
    private static final String LOG_TAG = "KNNTRAINMODEL";
    static final int NFEATURES = 500, NLEVELS = 8, EDGETHRESHOLD = 31, FIRSTLEVEL = 0,
            WTA_K = 2, SCORETYPE = ORB.FAST_SCORE, PATCHSIZE = 31, FASTTHRESHOLD = 20;
    static final float SCALEFACTOR = 1.2f;



    List<Tuple<Mat, String>> imageTupleList;
    int k = 11;
    Histogram<String> bestHist;

    public KnnWholeImageModel(){
        bestHist = new Histogram<>();
        imageTupleList = new ArrayList<>();
    }



    public KnnWholeImageModel(int k){
        this();
        this.k = k;
    }

    @Override
    public BaseModelInterface constructNew() {
        return new KnnWholeImageModel(k);
    }

    @Override
    public void trainAll(ListLabelTuple[] llts) {
        for(ListLabelTuple llt: llts){
            for(int i =0; i < llt.getFileNames().size(); i++){

                incrementalTrain(llt.getFileNames().get(i), llt.getLabel());
            }
        }
    }

    @Override
    public void incrementalTrain(String fileLocation, String label) {
        long start = System.currentTimeMillis();

        Mat newDesc = new Mat();
        Mat image = Imgcodecs.imread(fileLocation,Imgcodecs.IMREAD_REDUCED_GRAYSCALE_8);
        imageTupleList.add(new Tuple<Mat, String>(image, label));


        // Make sure to free up all allocated Mats we don't need anymore
//        image.release();
        Log.d(LOG_TAG, (System.currentTimeMillis() - start) + " milli to Train KNNWI" + k);

        newDesc.release();
    }



    @Override
    public String classify(String fileLocation) {

        long start = System.currentTimeMillis();
        bestHist.clear();
        Mat newDesc = new Mat();
        Mat image = Imgcodecs.imread(fileLocation,Imgcodecs.IMREAD_REDUCED_GRAYSCALE_8);
        Comparator<Tuple<String, Integer>> comparator = new Comparator<Tuple<String, Integer>>() {
            @Override
            public int compare(Tuple<String, Integer> stringIntegerTuple, Tuple<String, Integer> t1) {
                return Integer.compare(stringIntegerTuple.n2, t1.n2);
            }
        };
        PriorityQueue<Tuple<String, Integer>> pq = new PriorityQueue<>(imageTupleList.size(), comparator);
        int countCompares = 0;
        for(Tuple<Mat, String> tup: imageTupleList){
            if(tup.n1.rows() != image.rows() && tup.n1.cols() != image.cols()){
                Log.d(LOG_TAG, "image rc: " + image.rows() + " " + image.cols() + "this rc: " + tup.n1.rows() + " " + tup.n1.cols());
                Log.d(LOG_TAG, "Diff Dimensions got through " + countCompares);
            } else {
                countCompares++;
            }
            pq.add(new Tuple<String, Integer>(tup.n2, distance(tup.n1, image)));
        }
        for(int i = 0; i < k; i++){
            bestHist.bump(pq.poll().n1);
        }


        Log.d(LOG_TAG, (System.currentTimeMillis() - start) + " milli to Classify KNNWI" + k);

        // Mem releasing
        newDesc.release();
        image.release();
        return bestHist.getMax();
    }

    public static Integer distance(Mat m1, Mat m2){
        return (int) norm(m1,m2, Core.NORM_L1);
    }

    @Override
    public void dealloc() {
        bestHist.clear();
        for(Tuple<Mat, String> tup: imageTupleList){
            tup.n1.release();
        }
    }

}
