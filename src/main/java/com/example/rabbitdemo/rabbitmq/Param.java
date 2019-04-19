package com.example.rabbitdemo.rabbitmq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Param {
    private List<Map<String,String>> closeList = new ArrayList<>();

    public List<Map<String, String>> getCloseList() {
        return closeList;
    }

    public void setCloseList(List<Map<String, String>> closeList) {
        this.closeList = closeList;
    }
}
