package com.postmaninteractive.colorapp.Utils;

import android.graphics.Color;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexValidator {


    private static final String HEX_PATTERN = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"; // RegEx for hex patterns
    private Pattern pattern;
    private Matcher matcher;

    public HexValidator(){
        pattern = Pattern.compile(HEX_PATTERN);
    }

    /**
     * Validate a HEX code
     * @param colorString hex code to be validated
     * @return Validation
     */
    public boolean validate(String colorString){
        matcher = pattern.matcher(colorString);
        return matcher.matches();
    }
}
