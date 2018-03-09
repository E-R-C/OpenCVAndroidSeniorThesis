package edu.hendrix.huynhem.buildingopencv.Util;

import java.util.ArrayList;

/**
 *
 */

public interface AsyncResponse {
    void publishProgress(Integer... nums);
    void processFinish(ArrayList<String> output);
}
