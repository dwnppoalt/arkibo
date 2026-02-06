package org.arkibo.utils;

public class Logger {
    public static void log(String source, String message) {
        System.out.println(String.format("[%s]: %s", source, message));
    }
}
