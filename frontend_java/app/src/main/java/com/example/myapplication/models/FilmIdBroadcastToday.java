package com.example.myapplication.models;


import com.google.gson.annotations.SerializedName;

import java.util.List;

/*
    {
  "firm_ids": [
    10
  ]
}

 */
public class FirmIdBroadcastToday {
    @SerializedName("firm_ids")
    private List<Integer> firmIds;
    public FirmIdBroadcastToday(List<Integer> firmIds) {
        this.firmIds = firmIds;
    }

    public List<Integer> getFirmIds() {
        return firmIds;
    }

}
