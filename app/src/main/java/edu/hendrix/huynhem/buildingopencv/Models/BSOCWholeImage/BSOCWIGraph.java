package edu.hendrix.huynhem.buildingopencv.Models.BSOCWholeImage;

import android.util.Log;

import org.opencv.core.Mat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import edu.hendrix.huynhem.buildingopencv.Models.KnnWholeImageModel;
import edu.hendrix.huynhem.buildingopencv.Util.Histogram;
import edu.hendrix.huynhem.buildingopencv.Util.Tuple;

/**
 *
 */

public class BSOCWIGraph {
    Mat[] images;
    int[][] weights;
    int[] mergeCounts;
    ArrayList<Histogram<String>> labels;
    int clusters, indexptr;
    boolean filled = false;
    private static final int DEFAULT_CLUSTERS = 32;
    int dimr = 0, dimc = 0;
    public BSOCWIGraph(){
        this(DEFAULT_CLUSTERS);
    }

    public BSOCWIGraph(int clusters){
        images = new Mat[clusters];
        weights = new int[clusters][clusters];
        mergeCounts = new int[clusters];
        labels = new ArrayList<>(clusters);

        for(int i = 0; i < clusters; i++){
            labels.add(new Histogram<String>());
        }
        this.clusters =clusters;
        indexptr = 0;
    }

    public void addImage(Mat image, String label){
        if(!filled){
            if (dimc == dimr && dimr == 0){
                dimc = image.cols();
                dimr = image.rows();
            }
            labels.get(indexptr).bump(label);
            images[indexptr] = image;
            recalcEdges(indexptr);
            mergeCounts[indexptr] = 1;

            indexptr++;

            if(indexptr == clusters - 1){
                filled = true;
            }
        } else {
            if (images[indexptr] != null){
                images[indexptr].release();

            }
            images[indexptr] = image;
            labels.get(indexptr).bump(label);

            mergeCounts[indexptr] = 1;
            recalcEdges(indexptr);
            Tuple<Integer,Integer> twoNodes = getLowestWeightNodes();
            merge(twoNodes.n1, twoNodes.n2);
            recalcEdges(twoNodes.n1);
            indexptr = twoNodes.n2;

        }
    }

    private void merge(Integer n1, Integer n2) {
        int m1 = mergeCounts[n1];
        int m2 = mergeCounts[n2];
        double mtot = m1 + m2;
        double ratio1 = m1 / mtot;
        double ratio2 = m2 / mtot;

        for(int r = 0; r < images[n1].rows(); r++){
            for(int c = 0; c < images[n1].cols(); c++){
                int dist = (int) (images[n1].get(r,c)[0] * ratio1 + images[n2].get(r,c)[0] * ratio2);
                if (dist < 0){
                    Log.d("BSOCWI GRAPH", dist + "");
                }
                images[n1].put(r,c,new byte[]{(byte) dist});
            }
        }
        mergeCounts[n1] = m1 + m2;
        mergeCounts[n2] = 0;

        labels.get(n1).mergeInPlace(labels.get(n2));
        labels.get(n2).clear();

    }

    private void setEdgesToMaxInt(int i){
        for(int j = 0; j < weights.length; j++){
            weights[j][i] = Integer.MAX_VALUE;
            weights[i][j] = Integer.MAX_VALUE;
        }
    }



    // calculates the values for int i
    public void recalcEdges(int i){
        if(filled){
            for(int j = 0; j < weights.length; j++){
                if( j == i){
                    weights[j][i] = Integer.MAX_VALUE;
                } else {
                    int dist = KnnWholeImageModel.distance(images[j],images[i]);
                    weights[j][i] = dist;
                    weights[i][j] = dist;
                }
            }
        } else {
            for(int j = 0; j < indexptr; j++){
                weights[j][i] = KnnWholeImageModel.distance(images[j],images[i]);
                if (mergeCounts[i] > mergeCounts[j]){
                    weights[j][i] *= mergeCounts[i];
                } else {
                    weights[j][i] *= mergeCounts[j];
                }
                weights[i][j] = weights[j][i];
            }
        }
    }

    public Tuple<Integer, Integer> getLowestWeightNodes(){
        int n1 = 0, n2 = 0;
        int lowestWeight = Integer.MAX_VALUE;
        for(int r = 0; r < weights.length; r++){
            for(int  c = r + 1; c < weights[0].length; c++){
                if(weights[r][c] < lowestWeight){
                    n1 = r;
                    n2 = c;
                    lowestWeight = weights[r][c];
                }
            }
        }
        return new Tuple<>(n1, n2);
    }

    public void clear(){
        for(Mat m: images){
            if(m != null){
                m.release();
            }
        }

    }
}
