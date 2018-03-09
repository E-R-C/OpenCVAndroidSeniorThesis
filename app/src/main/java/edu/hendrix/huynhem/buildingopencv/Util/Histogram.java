package edu.hendrix.huynhem.buildingopencv.Util;

import java.util.HashMap;

/**
 *
 */

public class Histogram<K> {

    HashMap<K,Integer> map;

    public Histogram (){
        map = new HashMap<>();
    }

    public void bump(K k){
        if (!map.keySet().contains(k)){
            map.put(k,1);

        } else {
            map.put(k, map.get(k) + 1);
        }
    }

    public void clear(){
        map.clear();
    }

    public K getMax(){
        K bestK = null;
        int bestCount = 0;
        for(K key: map.keySet()){
            if (map.get(key) > bestCount){
                bestK = key;
                bestCount = map.get(key);
            }
        }
        return bestK;
    }

}
