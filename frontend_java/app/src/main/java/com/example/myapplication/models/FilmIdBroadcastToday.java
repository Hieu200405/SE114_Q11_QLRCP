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
public class FilmIdBroadcastToday {
    @SerializedName("firm_ids")
    private List<Integer> filmIds;
    public FilmIdBroadcastToday(List<Integer> filmIds) {
        this.filmIds = filmIds;
    }

    public List<Integer> getFilmIds() {
        return filmIds;
    }

}
