package com.example.demo.dto;

/**
 * @author DZQ
 * @date 2022/9/7 16:55
 */
public class moban {
    private String value;
    private String color;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public moban(String value, String color) {
        this.value = value;
        this.color = color;
    }
}
