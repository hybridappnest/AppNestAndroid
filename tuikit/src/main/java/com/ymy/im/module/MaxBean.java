package com.ymy.im.module;

import android.net.Uri;

import com.tencent.imsdk.v2.V2TIMVideoElem;

import java.io.Serializable;
import java.util.List;

public class MaxBean implements Serializable {
    private int index;
    private List<Data> data;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }
    public Data getNewData(){
        return  new Data();
    }
    public class Data implements Serializable {
        private String type;
        private String url;
        private String coverUrl;
        private String during;
        private int position;
        private String id;
        private int length;
        private V2TIMVideoElem videoElem;

        public V2TIMVideoElem getVideoElem() {
            return videoElem;
        }

        public void setVideoElem(V2TIMVideoElem videoElem) {
            this.videoElem = videoElem;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getCoverUrl() {
            return coverUrl;
        }

        public void setCoverUrl(String coverUrl) {
            this.coverUrl = coverUrl;
        }

        public String getDuring() {
            return during;
        }

        public void setDuring(String during) {
            this.during = during;
        }
    }
}
