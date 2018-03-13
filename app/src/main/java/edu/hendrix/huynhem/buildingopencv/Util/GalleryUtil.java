package edu.hendrix.huynhem.buildingopencv.Util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

/**
 *
 */

public class GalleryUtil {
    public static ArrayList<String> extractArrayList(Context c, Intent data){
        ArrayList<String> filenames = new ArrayList<>();
        if (data.getClipData() != null) {
            int numberOfImages = data.getClipData().getItemCount();
            for (int i = 0; i < numberOfImages; i++) {
                filenames.add(FilePather.getFilePath(c.getApplicationContext(), data.getClipData().getItemAt(i).getUri()));
            }

        }
        else {
            final Uri imageUri = data.getData();
            filenames.add(FilePather.getFilePath(c,imageUri));
        }
        return filenames;
    }
    public static String[] arraylistToStringarray(ArrayList<String> filesNames){
        String[] result = new String[filesNames.size()];
        for(int i = 0; i < filesNames.size(); i++){
            result[i] = filesNames.get(i);
        }
        return result;
    }
}
