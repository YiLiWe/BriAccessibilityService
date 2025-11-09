package com.xposed.briaccessibilityservice.runnable.response;

import lombok.Builder;
import lombok.Data;

//通用响应结构
@Builder
@Data
public class ResultResponse {
    private int code;
    private String msg;
    private Object data;

    //自定义数字
    private long digit;

    private final long timestamp = System.currentTimeMillis();

    public static ResultResponse success(String msg) {
        return ResultResponse.builder().code(Code.SUCCESS.code).msg(msg).build();
    }

    public static ResultResponse success(Object data) {
        return ResultResponse.builder().code(Code.SUCCESS.code).msg(Code.SUCCESS.msg).data(data).build();
    }

    public static ResultResponse error(String msg) {
        return ResultResponse.builder().code(Code.ERROR.code).msg(msg).build();
    }

    public static ResultResponse error(Code code) {
        return ResultResponse.builder().code(code.code).msg(code.msg).build();
    }

    public static ResultResponse error(int code, String msg) {
        return ResultResponse.builder().code(code).msg(msg).build();
    }


    public enum Code {
        SUCCESS(200, "成功"),
        ERROR(500, "失败"),
        UNAUTHORIZED(401, "未认证"),
        FORBIDDEN(403, "禁止访问"),
        ABNORMAL(400, "异常状态");  // 转换为大写并补充参数
        ;
        final int code;
        final String msg;

        Code(int i, String msg) {
            this.code = i;
            this.msg = msg;
        }
    }
}
