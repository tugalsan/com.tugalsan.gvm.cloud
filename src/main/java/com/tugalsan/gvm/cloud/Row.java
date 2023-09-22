package com.tugalsan.gvm.cloud;

import com.tugalsan.api.time.client.TGS_Time;

public class Row {

    private Row(String hash, String url, TGS_Time time) {
        this.hash = hash;
        this.url = url;
        this.time = time;
    }
    public String hash;
    public String url;
    public TGS_Time time;

    public static Row of(String hash, String url, TGS_Time time) {
        return new Row(hash, url, time);
    }

    @Override
    public String toString() {
        return Row.class.getSimpleName() + "{" + "time=" + time + ", hash=" + hash + ", url=" + url + '}';
    }
}
