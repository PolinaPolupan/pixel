package com.example.mypixel.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Scene implements Serializable {
    private String id;
}
