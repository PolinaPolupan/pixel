package com.example.mypixel.service;


public class FilteringServiceNative {

    public native void gaussianBlur(String filename, int sizeX, int sizeY, double sigmaX, double sigmaY);

    public native void medianBlur(String filename, int ksize);

    public native void blur(String filename, int sizeX, int sizeY);

    public native void bilateralFilter(String filename, int d, double sigmaColor, double sigmaSpace);

    public native void boxFilter(String filename, int ddepth, int ksizeX, int ksizeY);

    static {
        System.loadLibrary("native-processor");
    }
}
