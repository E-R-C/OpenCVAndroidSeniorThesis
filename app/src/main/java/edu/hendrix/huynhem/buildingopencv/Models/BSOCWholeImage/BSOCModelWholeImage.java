package edu.hendrix.huynhem.buildingopencv.Models.BSOCWholeImage;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.Comparator;
import java.util.PriorityQueue;

import edu.hendrix.huynhem.buildingopencv.Models.BSOC.BSOCGraph;
import edu.hendrix.huynhem.buildingopencv.Models.BSOC.BSOCMatNode;
import edu.hendrix.huynhem.buildingopencv.Models.BaseModelInterface;
import edu.hendrix.huynhem.buildingopencv.Models.KnnWholeImageModel;
import edu.hendrix.huynhem.buildingopencv.Models.ListLabelTuple;
import edu.hendrix.huynhem.buildingopencv.Util.Histogram;
import edu.hendrix.huynhem.buildingopencv.Util.Tuple;

/**
 *
 */

public class BSOCModelWholeImage implements BaseModelInterface {
    private static final String LOG_TAG = "BSOC_MODEL";
    static final int NFEATURES = 500, NLEVELS = 8, EDGETHRESHOLD = 31, FIRSTLEVEL = 0,
            WTA_K = 2, SCORETYPE = ORB.FAST_SCORE, PATCHSIZE = 31, FASTTHRESHOLD = 20;
    static final float SCALEFACTOR = 1.2f;

    static int default_clusters = 100;
    int clusters;
    Histogram<String> bestHist;
    BSOCWIGraph graph;

    public BSOCModelWholeImage(){
        this(default_clusters);
    }

    public BSOCModelWholeImage(int clusters){
        this.clusters = clusters;
        bestHist = new Histogram<>();
        graph = new BSOCWIGraph(clusters);
    }
    @Override
    public BaseModelInterface constructNew() {
        return new BSOCModelWholeImage(clusters);
    }

    @Override
    public void trainAll(ListLabelTuple[] llts) {
        long start = System.currentTimeMillis();

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
        graph.addImage(image,label);

        newDesc.release();
        Log.d(LOG_TAG, (System.currentTimeMillis() - start) + " milli Increment to Train BSOCWI " + clusters);

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
        PriorityQueue<Tuple<String, Integer>> pq = new PriorityQueue<>(graph.images.length, comparator);

        for(int i = 0; i < graph.images.length; i++){
            if(graph.images[i] != null && i != graph.indexptr){
                pq.add(new Tuple<String, Integer>(graph.labels.get(i).getMax(), KnnWholeImageModel.distance(image,graph.images[i])));
            }
        }



        image.release();
        newDesc.release();
//        Log.d(LOG_TAG, "index:  " + index + "count: " + count + "DMATlen: " + resultDMats.length);
        Log.d(LOG_TAG, (System.currentTimeMillis() - start) + " milli to Classify BSOC" + clusters);
        Log.d(LOG_TAG, bestHist.toString());
        return pq.poll().n1;
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
        if (bestHist != null){
            bestHist.clear();
        } if (graph != null){
            graph.clear();

        }
        Log.d(LOG_TAG, "Called BSOC Dealloc");
    }
}
