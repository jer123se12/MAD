package com.sp.chatmate;

import java.util.List;

public class folderModel {
    private List<Card> CL;
    private String name;
    private boolean isExpanded;

    public folderModel(String name, List<Card> CL){
        this.CL=CL;
        this.name=name;
        isExpanded=false;
    }
    public void setExpanded(boolean expanded){
        isExpanded=expanded;
    }

    public List<Card> getCL() {
        return CL;
    }

    public String getName() {
        return name;
    }
    public boolean getExpanded(){
        return isExpanded;
    }
}
