package edu.hendrix.huynhem.buildingopencv.Models.BSOC;

import org.opencv.core.Mat;

import edu.hendrix.huynhem.buildingopencv.Util.Histogram;

/**
 *
 */

public class BSOCMatNode {
    private static final int bits = 8;
    private static final int defaultMergeCount = 0;
    private Mat descriptor;
    private int[] counts;
    private Histogram<String> labelHistogram;
    private int mergeCount;
    public BSOCMatNode(Mat descriptor, String label){
        this.descriptor = descriptor;
        instantiateCounts();
        labelHistogram = new Histogram<>();
        labelHistogram.bump(label);
        mergeCount = defaultMergeCount;
    }

    public void setNewValues(Mat newDesc, String label){
        this.descriptor.release();
        descriptor = newDesc;
        instantiateCounts();
        labelHistogram.clear();
        labelHistogram.bump(label);
        mergeCount = defaultMergeCount;
    }


    private void instantiateCounts(){
        counts = new int[descriptor.cols() * bits];
        // Assumption: The clustList is an array of 32 floats representing 8bit ints
        for(int i = 0; i < counts.length; i++){
            int numi = i / descriptor.cols();
            int biti = i % bits;
            int num = (int) descriptor.get(0,numi)[0];
            // If this bit is set, then
            if ((num & (1 << biti)) > 0){
                counts[i] = 0;
                // Otherwise, we need to set it to -1;
            } else {
                counts[i] = -1;
            }
        }
    }

    public int[] getCounts(){
        return counts;
    }
    public Mat getDescriptor(){
        return descriptor;
    }
    public String getLabel(){
        return labelHistogram.getMax();
    }
    public void mergeInPlace(BSOCMatNode otherBsocNode){
//        Note use to modify the Mat descriptor.put();
        // Todo: Double check this with Dr. Ferrer
        int[] ocounts = otherBsocNode.getCounts();
        byte currentNum = 0;
        for(int i = 0; i < counts.length; i++){
            counts[i] += ocounts[i];
            int biti = i % bits;
            if (biti == 0){
                currentNum = 0;
            }
            if (counts[i] >= 0){
                currentNum |= (1 << biti);
            }
            if (biti == bits - 1){
                descriptor.put(0,i/bits,new byte[]{currentNum});
            }
        }
        // TODO Double check this merge counting system
        mergeCount += otherBsocNode.getMergeCount();
        labelHistogram.mergeInPlace(otherBsocNode.labelHistogram);
    }

    public int getMergeCount(){
        return mergeCount;
    }

    public void dealloc(){
        descriptor.release();
    }
}

