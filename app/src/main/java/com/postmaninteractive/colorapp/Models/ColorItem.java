package com.postmaninteractive.colorapp.Models;


public class ColorItem {

    private final String colorString ;
    private final String generalName;

    public ColorItem(String colorString, String generalName) {
        this.colorString = colorString;
        this.generalName = generalName;

    }

    public String getColorString() {
        return colorString;
    }

    public String getGeneralName() {
        return generalName;
    }


}
