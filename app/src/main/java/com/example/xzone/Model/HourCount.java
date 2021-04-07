package com.example.xzone.Model;

import java.util.ArrayList;
import java.util.List;

public class HourCount {

    List<String> hours;
    List<Integer> count;

    public HourCount() {
        this.hours = new ArrayList<>();
        this.count = new ArrayList<>();
    }

    public void setHours(List<String> hours) {
        this.hours = hours;
    }

    public void setCount(List<Integer> count) {
        this.count = count;
    }

    public List<String> getHours() {
        return hours;
    }

    public List<Integer> getCount() {
        return count;
    }
}
