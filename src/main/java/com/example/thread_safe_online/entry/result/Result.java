package com.example.thread_safe_online.entry.result;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    Integer code;

    String msg;

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static <T> Result<T> success(String msg) {
        return new Result<T>(200, msg);
    }

    public static <T> Result<T> fail(String msg){
        return new Result<T>(500, msg);
    }
}
