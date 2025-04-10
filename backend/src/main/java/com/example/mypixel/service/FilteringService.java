package com.example.mypixel.service;

public class FilteringService {

    public void gaussianBlur(String filename, int sizeX, int sizeY, double sigmaX, double sigmaY) {
        gaussianBlurNative(filename, sizeX, sizeY, sigmaX, sigmaY);
    }

    private native void gaussianBlurNative(String filename, int sizeX, int sizeY, double sigmaX, double sigmaY);

    static {
        System.loadLibrary("native-processor");
    }
}
