package com.skt.sf.rtti.util;

import java.util.Arrays;

public class Percentile {
 
    protected double[] data;
 
    public void setData(final double [] data) {
        Arrays.sort(data);
        this.data = data;
    }
 
    // see [https://en.wikipedia.org/wiki/Percentile_rank]
    public double rank(final double value) {
 
        if (data.length == 0) {
            throw new IllegalArgumentException("Data array is empty");
        }
 
        int lowerCount = 0;
        int sameCount = 0;
        int n = data.length;
        for (int i = 0; i<data.length; i++) {
            if (data[i] < value) {
                lowerCount++;
            } else if (data[i] == value) {
                sameCount++;
            } else {
                break;
            }
        }        
         
        if (sameCount == 0) {
            throw new IllegalArgumentException("Provided value do not exists in dataset: " + value);
        }
 
        return (lowerCount + 0.5 * sameCount) / n * 100;
    }
 
    public static void main(String[] args) {
        Percentile percentile = new Percentile();
 
        //double[] data = {1,3,5,7,7,7,7,8,8,9,10,11,12,13,14,15,16,17};
        double[] data = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25};

        //double[] data = {1,2,4};

        percentile.setData(data);

        for (double i : data) {
            System.out.println("Percentile("+ i +"): " + percentile.rank(i));
        }

        //System.out.println("7: " + percentile.rank(7));
    }
}

// https://szakuljarzuk.wordpress.com/2015/09/09/problem-how-to-calculate-percentile-rank/