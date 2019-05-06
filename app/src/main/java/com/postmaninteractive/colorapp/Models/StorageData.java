package com.postmaninteractive.colorapp.Models;

public class StorageData {

    private final String data;

    public StorageData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public class StorageDataResponse{
        private int id;
        private String data;

        public StorageDataResponse(int id, String data) {
            this.id = id;
            this.data = data;
        }

        public int getId() {
            return id;
        }

        public String getData() {
            return data;
        }
    }


}
