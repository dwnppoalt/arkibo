package org.arkibo.dto;

public record Response<T>(boolean ok, String message, T data) {

    public static <T> Response <T> success(String message, T data) {
        return new Response<>(true, message, data);
    }

    public static <T> Response <T> error(String message) {
        return new Response<>(false, message, null);
    }

}