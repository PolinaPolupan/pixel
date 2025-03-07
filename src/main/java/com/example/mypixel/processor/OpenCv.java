package com.example.mypixel.processor;

public class OpenCv {

    public native void hi();

    static {
        System.loadLibrary("native-processor");
    }
}
