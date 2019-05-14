package com.postmaninteractive.colorapp.Models;

public class StorageData {

    private final String data;      // Data stored

    /**
     * Created StorageData object
     * @param data Data to be stored
     */
    public StorageData(String data) {
        this.data = data;
    }

    /**
     * Get data stored in this object
     * @return Data stored
     */
    public String getData() {
        return data;
    }

    /**
     * StorageDataResponse class
     */
    public class StorageDataResponse{
        private final int id;         // Response will have an id
        private final String data;    // Response will have data

        public StorageDataResponse(int id, String data) {
            this.id = id;
            this.data = data;
        }

        /**
         * Get stored id
         * @return Id stored
         */
        public int getId() {
            return id;
        }

        /**
         * Get data stored
         * @return Data stored
         */
        public String getData() {
            return data;
        }
    }

}
