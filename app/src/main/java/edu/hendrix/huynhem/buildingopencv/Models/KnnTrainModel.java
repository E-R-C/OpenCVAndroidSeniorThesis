package edu.hendrix.huynhem.buildingopencv.Models;

import android.util.Log;

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

import edu.hendrix.huynhem.buildingopencv.Util.Histogram;

/**
 *
 */

public class KnnTrainModel implements BaseModelInterface {

    // Default create(int nfeatures = 500, float scaleFactor = 1.2f, int nlevels = 8, int edgeThreshold = 31, int firstLevel = 0, int WTA_K = 2, int scoreType = ORB::HARRIS_SCORE, int patchSize = 31, int fastThreshold = 20)
    private static final String LOG_TAG = "KNNTRAINMODEL";
    static final int NFEATURES = 500, NLEVELS = 8, EDGETHRESHOLD = 31, FIRSTLEVEL = 0,
            WTA_K = 2, SCORETYPE = ORB.FAST_SCORE, PATCHSIZE = 31, FASTTHRESHOLD = 20;
    static final float SCALEFACTOR = 1.2f;



    ORB orb;
    Mat descriptors;
    BFMatcher bfm;
    int k = 11;
    List<String> labels;
    Histogram<String> bestHist;

    public KnnTrainModel(){
        orb = ORB.create(NFEATURES,SCALEFACTOR,NLEVELS,EDGETHRESHOLD,FIRSTLEVEL,WTA_K,SCORETYPE,PATCHSIZE,FASTTHRESHOLD);
        descriptors  = new Mat();
        bfm = BFMatcher.create(BFMatcher.BRUTEFORCE_HAMMING,true);
        bestHist = new Histogram<>();
        labels = new ArrayList<>();
    }



    // The first arguement in strings should be the label, This is poor design, it should be changed later


    public KnnTrainModel(int k){
        this();
        this.k = k;
    }

    @Override
    public BaseModelInterface constructNew() {
        return new KnnTrainModel();
    }

    @Override
    public void trainAll(List<String> strings, String label) {
        for (int i = 0; i < strings.size(); i++){
            String s = strings.get(i);
            train(s, label);
        }
    }

    @Override
    public void train(String fileLocation, String label) {
        Mat newDesc = new Mat();
        Mat image = Imgcodecs.imread(fileLocation,Imgcodecs.IMREAD_REDUCED_GRAYSCALE_8);
        MatOfKeyPoint ignoredKeypoints = new MatOfKeyPoint();
        orb.detect(image,ignoredKeypoints);
        orb.compute(image,ignoredKeypoints,newDesc);
        descriptors.push_back(newDesc);

//        StringBuilder sb = new StringBuilder();
//        for(int i = 0; i < newDesc.cols(); i++){
//            sb.append(Arrays.toString(newDesc.get(0,i)));
//            sb.append(" ");
//        }
//        Log.d(LOG_TAG, sb.toString());

        // Interning could make the list take up less space
//        label = label.intern();
        labels.addAll(Collections.nCopies(newDesc.rows(), label));

        // Make sure to free up all allocated Mats we don't need anymore
        ignoredKeypoints.release();
        image.release();
        newDesc.release();
    }



    @Override
    public String classify(String fileLocation) {
        bestHist.clear();
        Mat newDesc = new Mat();
        Mat image = Imgcodecs.imread(fileLocation,Imgcodecs.IMREAD_REDUCED_GRAYSCALE_8);
        MatOfKeyPoint ignoredKeypoints = new MatOfKeyPoint();
        orb.detect(image,ignoredKeypoints);
        orb.compute(image,ignoredKeypoints,newDesc);
        MatOfDMatch results = new MatOfDMatch();
        bfm.match(newDesc,descriptors,results);
        DMatch[] resultDMats = results.toArray();
        Arrays.sort(resultDMats, new Comparator<DMatch>() {
            @Override
            public int compare(DMatch dMatch, DMatch t1) {
                return Float.compare(dMatch.distance, t1.distance);
            }
        });

        for(int i = 0; i < this.k; i++){
            bestHist.bump(labels.get(resultDMats[i].trainIdx));
        }

        // Mem releasing
        results.release();
        newDesc.release();
        image.release();
        return bestHist.getMax();
    }

}
