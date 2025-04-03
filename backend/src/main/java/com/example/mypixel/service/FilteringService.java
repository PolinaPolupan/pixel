package com.example.mypixel.service;


import com.example.mypixel.exception.InvalidNodeParameter;

public class FilteringService {

    public void gaussianBlur(String filename, int sizeX, int sizeY, double sigmaX, double sigmaY) {
        if (sizeX < 0 || sizeX % 2 == 0) {
            throw new InvalidNodeParameter("SizeX must be positive and odd");
        }
        if (sizeY < 0 || sizeY % 2 == 0) {
            throw new InvalidNodeParameter("SizeY must be positive and odd");
        }
        if (sigmaX < 0) {
            throw new InvalidNodeParameter("SigmaX must be positive");
        }
        if (sigmaY < 0) {
            throw new InvalidNodeParameter("SigmaY must be positive");
        }
        gaussianBlurNative(filename, sizeX, sizeY, sigmaX, sigmaY);
    }

    private native void gaussianBlurNative(String filename, int sizeX, int sizeY, double sigmaX, double sigmaY);

    static {
        System.loadLibrary("native-processor");
    }
}
