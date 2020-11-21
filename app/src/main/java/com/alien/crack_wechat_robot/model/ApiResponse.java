package com.alien.crack_wechat_robot.model;

public class ApiResponse {

    private boolean ret;
    private int code;
    private Object data;
    private String message;

    public static ApiResponse success(Object data) {
        ApiResponse apiResponse = new ApiResponse();
        return apiResponse.suc(data);
    }

    public static ApiResponse failed(String message) {
        ApiResponse apiResponse = new ApiResponse();
        return apiResponse.fail(message);
    }

    public ApiResponse fail(String message) {
        this.message = message;
        this.ret = false;
        this.code = -1;
        return this;
    }

    public ApiResponse suc(Object data) {
        this.ret = true;
        this.code = 0;
        this.data = data;
        return this;
    }

    public boolean isRet() {
        return ret;
    }

    public void setRet(boolean ret) {
        this.ret = ret;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "ret=" + ret +
                ", code=" + code +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }
}
