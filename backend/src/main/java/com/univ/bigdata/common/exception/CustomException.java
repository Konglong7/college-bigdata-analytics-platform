package com.univ.bigdata.common.exception;

import com.univ.bigdata.common.api.ResultCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final int code;

    public CustomException(String message) {
        super(message);
        this.code = ResultCode.FAILED.getCode();
    }

    public CustomException(int code, String message) {
        super(message);
        this.code = code;
    }

    public CustomException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
}
