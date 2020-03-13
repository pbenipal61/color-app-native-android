package com.postmaninteractive.colorapp.Models;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ColorItemTest {

    private String colorString = "#fff";
    private String generalName = "White";
    private ColorItem colorItem;
    @BeforeEach
    void setUp() {
        colorItem = new ColorItem(colorString, generalName);

    }

    /**
     * Test for getter for colorString
     */
    @Test
    void testColorString(){
        Assert.assertEquals(colorString, colorItem.getColorString());
    }

    /**
     * Test for getter for generalName
     */
    @Test
    void testGeneralName(){
        Assert.assertEquals(generalName, colorItem.getGeneralName());
    }

    /**
     * Test for incorrect result expectation from the getter
     */
    @Test
    void testIncorrectExpectation(){
        Assert.assertNotSame(colorString, generalName);
    }
}