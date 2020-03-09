package com.postmaninteractive.colorapp.Utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class HexValidatorTest {

    private HexValidator hexValidator;
    private String colorString;

    @BeforeEach
    void setUp(){
        hexValidator = new HexValidator();
    }

    @Test
    void testIncorrectLength(){
        colorString = "#FFFF";
        Assert.assertFalse(hexValidator.validate(colorString));
    }

    @Test
    void testIncorrectPrefix(){
        colorString = "FFF";
        Assert.assertFalse(hexValidator.validate(colorString));
    }

    @Test
    void testInvalidCharacter(){
        colorString = "FFH";
        Assert.assertFalse(hexValidator.validate(colorString));
    }

    @Test
    void testValidShortCode(){
        colorString="#FFF";
        Assert.assertTrue(hexValidator.validate(colorString));
    }

    @Test
    void testValidLongCode(){
        colorString = "#FFFFFF";
        Assert.assertTrue(hexValidator.validate(colorString));
    }

    @Test
    void testNumbersOnlyCode(){
        colorString = "#000000";
        Assert.assertTrue(hexValidator.validate(colorString));
    }
}