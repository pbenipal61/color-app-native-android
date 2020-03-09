package com.postmaninteractive.colorapp.Models;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorItemTest {

    private String colorString = "#fff";
    private String generalName = "White";
    private ColorItem colorItem;
    @BeforeEach
    void setUp() {
        colorItem = new ColorItem(colorString, generalName);

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testColorString(){
        Assert.assertEquals(colorString, colorItem.getColorString());
    }

    @Test
    void testGeneralName(){
        Assert.assertEquals(generalName, colorItem.getGeneralName());
    }
}