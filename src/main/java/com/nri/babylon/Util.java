package com.nri.babylon;


public class Util {
    private static Util instance;
    private final String recorderEndpointIpAddress;

    public Util(String recorderEndpointIpAddress) {
        this.recorderEndpointIpAddress = recorderEndpointIpAddress;
    }


    public static void setInstance(Util util) {
        instance = util;
    }

    public static String recorderEndpointIpAddress(){
        return instance.getRecorderEndpointIpAddress();
    }

    private String getRecorderEndpointIpAddress() {
        return recorderEndpointIpAddress;
    }
}
