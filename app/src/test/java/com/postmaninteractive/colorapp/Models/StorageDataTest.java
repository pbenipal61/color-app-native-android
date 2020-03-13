package com.postmaninteractive.colorapp.Models;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StorageDataTest {

    private String data = "kjhvjh";
    private StorageData storageData;

    @BeforeEach
    void setUp() {
        storageData = new StorageData(data);

    }

    /**
     * Test for data getter
     */
    @Test
    void testData(){
        Assert.assertEquals(data, storageData.getData());
    }
}