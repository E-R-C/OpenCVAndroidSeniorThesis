package edu.hendrix.huynhem.buildingopencv.Models.BSOC;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;


import edu.hendrix.huynhem.buildingopencv.Util.Tuple;

/**
 *
 */

public class BSOCGraph {
    private static final String LOG_TAG = "BSOC Graph";
    private static final int defaultNodes = 100;

    BSOCMatNode[] nodes;
    // We can always assume that the buffer index is not a node in our graph
    int bufferIndex = 0;
    int nodeCount = 0;
    EdgePriorityQueue epq;
    public BSOCGraph(){
        this(defaultNodes);
    }
    public BSOCGraph(int numNodes){
        nodes = new BSOCMatNode[numNodes];
        epq = new EdgePriorityQueue(numNodes);
    }


    public BSOCMatNode getNearestNeighbor(Mat toTest){
        int bestDist = Integer.MAX_VALUE;
        int bestIndex = -1;

        for(int i = 0; i < nodes.length; i++){
            if (i != bufferIndex){
                int dist = calculateHamming(toTest, nodes[i].getDescriptor());
                if (dist < bestDist) {
                    bestDist = dist;
                    bestIndex = i;
                }
            }
        }
        return nodes[bestIndex];
    }

    public void add(Mat dArray, String label){
        // if we  have less nodes than total will fit, then
        if (nodeCount < nodes.length - 1){
            // make a new node,
            BSOCMatNode newNode = new BSOCMatNode(dArray, label);

            // add it into the graph
            addNode(newNode);
            // update the counters
            nodeCount++;
            bufferIndex++;
        } else {
            // otherwise, we go to the node at bufferIndex (our spare node) and we make it the new node
            if (nodes[bufferIndex] == null){
                BSOCMatNode newNode = new BSOCMatNode(dArray, label);
                addNode(newNode);
                Log.d(LOG_TAG, "Should happen once");
            } else {
                nodes[bufferIndex].setNewValues(dArray, label);
                addNode(nodes[bufferIndex]);
            }

            nodeCount++;
        }
    }

    private void addNode(BSOCMatNode node){
        nodes[bufferIndex] = node;
        insecureInsert(bufferIndex, node);
        if (nodeCount >= nodes.length - 1){
            // If we are full, i.e nodeCount == nodes.length - 1;
            // We'll need to merge two nodes together
            Tuple<Integer, Integer> toMerge = getTwoClosest();
//            Log.d(LOG_TAG , " n1 = " + toMerge.n1 + " n2 = " + toMerge.n2);

            mergeNodes(toMerge.n1, toMerge.n2);
        }
    }

    private Tuple<Integer, Integer> getTwoClosest(){
        return epq.getNextTuple();
    }

    public int getIndexToSkip(){
        return bufferIndex;
    }

    public Mat getDescriptorsAsMat(){
        return getDescriptorsAsMat(nodes);
    }
    private Mat getDescriptorsAsMat(BSOCMatNode[] nodeslist){
        Mat result = new Mat();
        for(int i = 0; i < nodeslist.length; i++){
            if ( i != bufferIndex ){
                result.push_back(nodeslist[i].getDescriptor());
            } else {
                // this needs to be handled cleaner, basically, if i == bufferindex one is chosen, ignore it.
                if (bufferIndex == 0){
                    result.push_back(nodeslist[1].getDescriptor());
                } else {
                    result.push_back(nodeslist[0].getDescriptor());
                }
            }
        }
        return result;
    }
    private void insecureInsert(int index, BSOCMatNode node){
        nodes[index] = node;
        int limit = Math.min(nodeCount , nodes.length);
        for(int i = 0; i < limit; i++){
            if (i != index && i != bufferIndex){
//                if(nodes[i] == null){
//                    Log.d(LOG_TAG, "BufferINdex = " + bufferIndex + " index = " + index + " i = " + i);
//                    Log.d(LOG_TAG, i + " ");
//                }
                int weight = 1 + calculateHamming( nodes[index], nodes[i]);
                weight *= Math.max(nodes[i].getMergeCount() , nodes[index].getMergeCount());
                epq.pushEdge(i, index, weight);
//                Log.d(LOG_TAG, "Pushed weight " + weight);
//                epq.logPeekWeight();
            }
        }
//        Log.d(LOG_TAG, "FINISHED INSERT");
    }


    private void mergeNodes(int index1, int index2){
//        Log.d(LOG_TAx`G, "Merging: " + index1 + " " + index2);
        nodes[index1].mergeInPlace(nodes[index2]);
        eraseNodeDists(index2);
        eraseNodeDists(index1);
        nodes[index2].dealloc();
        bufferIndex = index2;
        insecureInsert(index1, nodes[index1]);

//        nodeCount--;
    }

    private void eraseNodeDists(int index){
        epq.removeNode(index);
    }

    private int calculateHamming(Mat one, Mat two){
        double dist = Core.norm(one, two, Core.NORM_HAMMING);
        return (int) dist;
//        int total = 0;
//        for(int i = 0; i < one.cols(); i++){
//            total += (Integer.bitCount(((int) one.get(0,i)[0]) ^ ((int) two.get(0,i)[0])));
//        }
//        return total;
    }

    private int calculateHamming(BSOCMatNode one, BSOCMatNode two){
        return calculateHamming(one.getDescriptor(), two.getDescriptor());
    }
    public void clear(){
        epq.clear();
        nodes = null;
    }
}
