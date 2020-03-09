package com.postmaninteractive.colorapp.Models;


public class ColorItem {

    private final String colorString ;      // HEX code for color as a string
    private final String generalName;       // General name of the color

    public ColorItem(String colorString, String generalName) {
        this.colorString = colorString;
        this.generalName = generalName;

    }

    /**
     * Get stored color string
     * @return Color string
     */
    public String getColorString() {
        return colorString;
    }

    /**
     * Get color name stored
     * @return General color name stored
     */
    public String getGeneralName() {
        return generalName;
    }




}
