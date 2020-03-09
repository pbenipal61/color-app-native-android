package com.postmaninteractive.colorapp.Models;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StorageDataTest {

    private String data = "kjhvjh";
    private StorageData storageData;

    @BeforeEach
    void setUp() {
        storageData = new StorageData(data);

    }

    @Test
    void testData(){
        Assert.assertEquals(data, storageData.getData());
    }
}