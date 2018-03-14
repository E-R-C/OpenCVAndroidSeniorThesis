package edu.hendrix.huynhem.buildingopencv.Models.BSOC;

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
        if (nodeCount < nodes.length){
            BSOCMatNode newNode = new BSOCMatNode(dArray, label);
            nodeCount++;
            addNode(newNode);
        } else {
            nodes[bufferIndex].setNewValues(dArray, label);
            addNode(nodes[bufferIndex]);
        }
    }

    private void addNode(BSOCMatNode node){
        nodes[bufferIndex] = node;
        if (bufferIndex == nodes.length - 1){
            // If we have one free space, then we want to add our node to the graph
            insecureInsert(bufferIndex, node);
            Tuple<Integer, Integer> toMerge = getTwoClosest();
            mergeNodes(toMerge.n1, toMerge.n2);
            bufferIndex = toMerge.n2;
        } else {
            insecureInsert(bufferIndex, node);
            bufferIndex++;
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
    private static Mat getDescriptorsAsMat(BSOCMatNode[] nodeslist){
        Mat result = new Mat();
        for(int i = 0; i < nodeslist.length; i++){
            result.push_back(nodeslist[i].getDescriptor());
        }
        return result;
    }
    private void insecureInsert(int index, BSOCMatNode node){
        nodes[index] = node;
        for(int i = 0; i < bufferIndex; i++){
            if (i != index){
                int weight = calculateHamming(nodes[i], nodes[index]);
                weight *= nodes[i].getMergeCount() > nodes[index].getMergeCount() ? nodes[i].getMergeCount() : nodes[index].getMergeCount();
                if (i < index){
                    epq.pushEdge(i, index, weight);
                } else if (i > index){
                    epq.pushEdge(index, i, weight);
                }
            }

        }
    }


    private void mergeNodes(int index1, int index2){
        nodes[index1].mergeInPlace(nodes[index2]);
        nodes[index2].dealloc();
        eraseNodeDists(index2);
        bufferIndex = index2;
    }

    private void eraseNodeDists(int index){
        epq.removeNode(index);
    }

    private static int calculateHamming(Mat one, Mat two){
        int total = 0;
        for(int i = 0; i < one.cols(); i++){
            total += (Integer.bitCount(((int) one.get(0,i)[0]) ^ ((int) two.get(0,i)[0])));
        }
        return total;
    }

    private static int calculateHamming(BSOCMatNode one, BSOCMatNode two){
        return calculateHamming(one.getDescriptor(), two.getDescriptor());
    }

}
