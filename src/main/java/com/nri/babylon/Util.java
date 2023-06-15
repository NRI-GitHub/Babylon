package com.nri.babylon;


import com.nri.babylon.config.ColorGenerator;

public class Util {
    private static Util instance;
    private final String recorderEndpointIpAddress;
    private final ColorGenerator colorGenerator;

    public Util(String recorderEndpointIpAddress, ColorGenerator colorGenerator) {
        this.recorderEndpointIpAddress = recorderEndpointIpAddress;
        this.colorGenerator = colorGenerator;
    }


    public static void setInstance(Util util) {
        instance = util;
    }

    public static String recorderEndpointIpAddress(){
        return instance.getRecorderEndpointIpAddress();
    }

    public static String getUniqueHexColor(String name) {
        return instance.getColorGenerator().getUniqueHexColor(name);
    }

    private String getRecorderEndpointIpAddress() {
        return recorderEndpointIpAddress;
    }

    public ColorGenerator getColorGenerator() {
        return colorGenerator;
    }
}
