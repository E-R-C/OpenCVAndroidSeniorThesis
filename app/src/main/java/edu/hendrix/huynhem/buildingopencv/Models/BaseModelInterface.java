package edu.hendrix.huynhem.buildingopencv.Models;

import java.util.List;

/**
 *
 */

public interface BaseModelInterface {

    BaseModelInterface constructNew();
    void trainAll(List<String> strings, String label);
    void train(String fileLocation, String label);
    String classify(String fileLocation);
}
