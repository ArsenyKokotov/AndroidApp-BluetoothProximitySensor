package com.example.xzone.Model;

import java.util.ArrayList;
import java.util.List;
//USED TO RETRIEVE DATA FROM DATABASE
public class DayCount {
      List<String> days;
      List<Integer> count;

    public DayCount() {
        this.days =new ArrayList<>();
        this.count =new ArrayList<>();
    }

    public List<String> getDays() {
        return days;
    }

    public List<Integer> getCount() {
        return count;
    }

    public void setDays(List<String> days) {
        this.days = days;
    }

    public void setCount(List<Integer> count) {
        this.count = count;
    }
}
