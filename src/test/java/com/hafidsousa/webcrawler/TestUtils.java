package com.hafidsousa.webcrawler;

/**
 * @author Hafid Ferreira Sousa
 */
public class TestUtils {

    public static String seedBody = "<html><head><title>Seed URL</title></head><body>" +
            "<a href=\"/example/inner1\"></a>" +
            "</body</html>";

    public static String inner1 = "<html><head><title>Inner 1</title></head><body>" +
            "<a href=\"/example/inner1/inner11\"></a>" +
            "</body</html>";

    public static String inner11 = "<html><head><title>Inner 11</title></head><body>" +
            "<a href=\"/example/inner1/inner11/inner111\" ></a>" +
            "</body</html>";

    public static String inner111 = "<html><head><title>Inner 111</title></head><body>" +
            "</body</html>";

}
