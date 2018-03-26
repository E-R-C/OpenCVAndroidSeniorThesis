package edu.hendrix.huynhem.buildingopencv.Models.BSOC;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.List;

import edu.hendrix.huynhem.buildingopencv.Models.BaseModelInterface;
import edu.hendrix.huynhem.buildingopencv.Models.ListLabelTuple;
import edu.hendrix.huynhem.buildingopencv.Util.Histogram;

/**
 *
 */

public class BSOCModel implements BaseModelInterface {
    private static final String LOG_TAG = "BSOC_MODEL";
    static final int NFEATURES = 500, NLEVELS = 8, EDGETHRESHOLD = 31, FIRSTLEVEL = 0,
            WTA_K = 2, SCORETYPE = ORB.FAST_SCORE, PATCHSIZE = 31, FASTTHRESHOLD = 20;
    static final float SCALEFACTOR = 1.2f;

    static int default_clusters = 100;
    int clusters;
    ORB orb;
    BFMatcher bfm;
    List<String> labels;
    Histogram<String> bestHist;
    BSOCGraph graph;

    public BSOCModel(){
        this(default_clusters);
    }

    public BSOCModel(int clusters){
        this.clusters = clusters;
        orb = ORB.create(NFEATURES,SCALEFACTOR,NLEVELS,EDGETHRESHOLD,FIRSTLEVEL,WTA_K,SCORETYPE,PATCHSIZE,FASTTHRESHOLD);
        bfm = BFMatcher.create(BFMatcher.BRUTEFORCE_HAMMING,true);
        bestHist = new Histogram<>();
        labels = new ArrayList<>();
        graph = new BSOCGraph(clusters);
    }
    @Override
    public BaseModelInterface constructNew() {
        return new BSOCModel(clusters);
    }

    @Override
    public void trainAll(ListLabelTuple[] llts) {
        long start = System.currentTimeMillis();

        for(ListLabelTuple llt: llts){
            for(int i =0; i < llt.getFileNames().size(); i++){
                incrementalTrain(llt.getFileNames().get(i), labels.get(i));
            }
        }

        Log.d(LOG_TAG, (System.currentTimeMillis() - start) + " milli Total to Train BSOC" + clusters);
    }

    @Override
    public void incrementalTrain(String fileLocation, String label) {
        long start = System.currentTimeMillis();
        Mat newDesc = new Mat();
        Mat image = Imgcodecs.imread(fileLocation,Imgcodecs.IMREAD_REDUCED_GRAYSCALE_8);
        MatOfKeyPoint ignoredKeypoints = new MatOfKeyPoint();
        orb.detect(image,ignoredKeypoints);
        orb.compute(image,ignoredKeypoints,newDesc);
        for(int r = 0; r < newDesc.rows(); r++){
            Mat dest = new Mat(1,8, CvType.CV_8U);
            Mat temp = newDesc.row(r);
            temp.convertTo(dest,CvType.CV_8U);
            graph.add(dest, label);
        }
        ignoredKeypoints.release();
        image.release();
        newDesc.release();
        Log.d(LOG_TAG, (System.currentTimeMillis() - start) + " milli Increment to Train BSOC " + clusters);
    }

    @Override
    public String classify(String fileLocation) {
        long start = System.currentTimeMillis();
        bestHist.clear();
        Mat newDesc = new Mat();
        Mat image = Imgcodecs.imread(fileLocation,Imgcodecs.IMREAD_REDUCED_GRAYSCALE_8);
        MatOfKeyPoint ignoredKeypoints = new MatOfKeyPoint();
        orb.detect(image,ignoredKeypoints);
        orb.compute(image,ignoredKeypoints,newDesc);
        for(int r = 0; r < newDesc.rows(); r++){
            bestHist.bump(graph.getNearestNeighbor(newDesc.row(r)).getLabel());
        }

        ignoredKeypoints.release();
        image.release();
        newDesc.release();
        Log.d(LOG_TAG, (System.currentTimeMillis() - start) + " milli to Classify BSOC" + clusters);
        return bestHist.getMax();
    }

    /*
    clusters:
    32
    64
    128

    switch to array


     */


    @Override
    public void dealloc() {
        bestHist.clear();
        labels.clear();
        orb.clear();
        bfm.clear();
        graph.clear();
        Log.d(LOG_TAG, "Called BSOC Dealloc");
    }
}
