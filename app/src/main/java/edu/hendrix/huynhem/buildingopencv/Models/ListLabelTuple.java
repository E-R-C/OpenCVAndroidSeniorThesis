package edu.hendrix.huynhem.buildingopencv.Models;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 */

public class ListLabelTuple {
    private ArrayList<String> fileNames;
    private String label;
    public ListLabelTuple(ArrayList<String> filenames, String label){
        this.label = label;
        this.fileNames = filenames;
    }
    public ArrayList<String> getFileNames() {
        return fileNames;
    }

    public String getLabel() {
        return label;
    }



    public void setFileNames(ArrayList<String> fileNames) {
        this.fileNames = fileNames;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(String file:fileNames){
            sb.append(sb);
        }
        return sb.toString();
    }
}
