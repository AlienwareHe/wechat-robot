package com.alien.crack_wechat_robot.model;

public class InteractionResult<T> {

    private Boolean ret;

    private T data;

    private String msg;

    private int code;

    private static final String ignore = "ignore";

    public InteractionResult() {
    }

    public InteractionResult(Boolean ret, T data, String msg, int code) {
        this.ret = ret;
        this.data = data;
        this.msg = msg;
        this.code = code;
    }

    /**
     * 忽略会话, 不作回应
     */
    public static <T> InteractionResult<T> ignore() {
        return new InteractionResult<>(true, null, ignore, 1000);
    }

    public static <T> InteractionResult<T> success() {
        return new InteractionResult<>(true, null, "success", 1001);
    }

    public static <T> InteractionResult<T> success(T data, String message) {
        return new InteractionResult<>(true, data, null, 1001);
    }

    public static <T> InteractionResult<T> success(T data) {
        return InteractionResult.success(data, null);
    }

    public static <T> InteractionResult<T> fail(String message) {
        return new InteractionResult<>(false, null, message, 9999);
    }

    public Boolean getRet() {
        return ret;
    }

    public void setRet(Boolean ret) {
        this.ret = ret;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static String getIgnore() {
        return ignore;
    }
}
