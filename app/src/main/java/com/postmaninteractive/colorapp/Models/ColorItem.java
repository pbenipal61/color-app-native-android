package com.postmaninteractive.colorapp.Models;


import com.postmaninteractive.colorapp.Utils.HexValidator;

public class ColorItem {

    private final String colorString ;      // HEX code for color as a string
    private final String generalName;       // General name of the color
    private HexValidator validator;         // Hex validator
    private Boolean IsValid;                // If colorString is a valid hex code

    public ColorItem(String colorString, String generalName) {
        this.colorString = colorString;
        this.generalName = generalName;
        validator = new HexValidator();
        IsValid = validator.validate(colorString);

    }

    /**
     * Get stored color string
     * @return Color string
     */
    public String getColorString() {
        return colorString;

    }

    /**
     * Get validation for color string stored in this instance
     * @return Validation
     */
    public Boolean isValid(){
        return IsValid;
    }

    /**
     * Get color name stored
     * @return General color name stored
     */
    public String getGeneralName() {
        return generalName;
    }




}
