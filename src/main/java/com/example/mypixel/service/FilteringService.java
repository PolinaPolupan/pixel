package com.example.mypixel.service;


public class FilteringService {

    public native void gaussianBlur(String filename);

    static {
        System.loadLibrary("native-processor");
    }
}
