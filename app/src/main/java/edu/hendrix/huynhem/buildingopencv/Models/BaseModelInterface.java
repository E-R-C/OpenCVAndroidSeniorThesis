package edu.hendrix.huynhem.buildingopencv.Models;

import java.util.List;

/**
 *
 */

public interface BaseModelInterface {

    BaseModelInterface constructNew();
    void trainAll(ListLabelTuple[] fileLabels);
    void incrementalTrain(String fileLocation, String label);
    String classify(String fileLocation);
    void dealloc();
}
