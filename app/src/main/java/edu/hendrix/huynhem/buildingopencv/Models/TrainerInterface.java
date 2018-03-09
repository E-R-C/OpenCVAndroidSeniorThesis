package edu.hendrix.huynhem.buildingopencv.Models;

import java.util.List;

/**
 *
 */

public interface TrainerInterface {

    public void trainAll(List<String> strings, String label);
    public void train(String fileLocation, String label);
    public String classify(String fileLocation);
}
