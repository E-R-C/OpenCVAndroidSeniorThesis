package edu.hendrix.huynhem.buildingopencv.Models.BSOC;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Stack;

import edu.hendrix.huynhem.buildingopencv.Util.Tuple;

/**
 *
 */

public class EdgePriorityQueue {

    PriorityQueue<Edge> pq;
    Stack<Edge> unusedEdges;
    SparseArray<HashSet<Edge>> nodeToEdges;


    public EdgePriorityQueue(int numNodes){
        int totalCapacity = numNodes * numNodes;
        pq = new PriorityQueue<>(totalCapacity);
        unusedEdges = new Stack<>();
        for(int i = 0; i < totalCapacity; i++){
            unusedEdges.push(new Edge(0,0,0));
        }
        nodeToEdges = new SparseArray<>(numNodes);
        for(int i = 0; i < numNodes; i++){
            nodeToEdges.put(i,new HashSet<Edge>(numNodes));
        }
    }

    public Tuple<Integer, Integer> getNextTuple(){
        Edge e = pq.poll();
        unusedEdges.push(e);
        return new Tuple<>(e.i1, e.i2);
    }

    public void pushEdge(int start, int end, int weight){
        Edge e;
        if(unusedEdges.empty()){
             e = new Edge(start,end,weight);
        } else {
            e = unusedEdges.pop();
            e.i1 = start;
            e.i2 = end;
            e.weight = weight;
        }

        nodeToEdges.get(start).add(e);
        nodeToEdges.get(end).add(e);

        pq.add(e);
    }

    public void removeNode(int index){
        HashSet<Edge> nodes = nodeToEdges.get(index);
        for(Edge e : nodes){
            unusedEdges.push(e);
            pq.remove(e);
            int start = e.i1;
            int end = e.i2;
            if (start != index){
                nodeToEdges.get(start).remove(e);
            } else {
                nodeToEdges.get(end).remove(e);
            }
        }
        nodes.clear();
    }

    public void clear(){
        nodeToEdges.clear();
        unusedEdges.clear();
        pq.clear();
    }
    private class Edge implements Comparable<Edge>{
        int i1, i2, weight;
        Edge(int i1, int i2, int weight){
            this.i1 = i1;
            this.i2 = i2;
            this.weight = weight;
        }

        @Override
        public int compareTo(Edge edge) {
            return Integer.compare(weight, edge.weight);
        }
    }

}
