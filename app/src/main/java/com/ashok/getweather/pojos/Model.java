package com.ashok.getweather.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Model {

    @SerializedName("query")
    @Expose
    private Query query;

    /**
     * getter - @return  query
     */
    public Query getQuery() {
        return query;
    }

    /**
     * setter - @param query
     */
    public void setQuery(Query query) {
        this.query = query;
    }

}
