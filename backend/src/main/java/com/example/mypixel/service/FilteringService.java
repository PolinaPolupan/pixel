package com.example.mypixel.service;


import com.example.mypixel.model.Vector2D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FilteringService {

    private final FilteringServiceNative filteringServiceNative;

    @Autowired
    public FilteringService(FilteringServiceNative filteringServiceNative) {
        this.filteringServiceNative = filteringServiceNative;
    }

    public void gaussianBlur(String filename, int sizeX, int sizeY, double sigmaX, double sigmaY) {
        filteringServiceNative.gaussianBlur(filename, sizeX, sizeY, sigmaX, sigmaY);
    }

    public void medianBlur(String filename, int ksize) {
        filteringServiceNative.medianBlur(filename, ksize);
    }

    public void blur(String filename, int sizeX, int sizeY) {
        filteringServiceNative.blur(filename, sizeX, sizeY);
    }

    public void bilateralFilter(String filename, int d, double sigmaColor, double sigmaSpace) {
        filteringServiceNative.bilateralFilter(filename, d, sigmaColor, sigmaSpace);
    }

    public void boxFilter(String filename, int ddepth, int ksize, int ksizeY) {
        filteringServiceNative.boxFilter(filename, ddepth, ksize, ksizeY);
    }

    public void blur(String filename, Vector2D<Number> ksize) {
        int ksizeX = ksize.getX().intValue();
        int ksizeY = ksize.getY().intValue();

        blur(filename, ksizeX, ksizeY);
    }

    public void boxFilter(String filename, int ddepth, Vector2D<Number> ksize) {
        int ksizeX = ksize.getX().intValue();
        int ksizeY = ksize.getY().intValue();

        boxFilter(filename, ddepth, ksizeX, ksizeY);
    }
}
