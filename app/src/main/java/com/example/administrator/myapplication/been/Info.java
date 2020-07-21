package com.example.administrator.myapplication.been;

public class Info {
    private int portraitId;//头像id
    private String name="";//名字
    private String kind="";
    private float distance=0;//距离

    public int getPortraitId() {
        return portraitId;
    }

    public void setPortraitId(int portraitId) {
        this.portraitId = portraitId;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

     public String getKind(){ return  kind;}

    public void setKind(String kind) {
        this.kind = kind;
    }
}
