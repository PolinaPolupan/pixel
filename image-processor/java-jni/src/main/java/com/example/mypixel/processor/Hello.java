package com.example.mypixel.processor;

import java.lang.*;

public class Hello {

    public native void sayHello();

    static {
        System.loadLibrary("native-processor");
    }
}