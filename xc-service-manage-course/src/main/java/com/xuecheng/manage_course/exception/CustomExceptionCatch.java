package com.xuecheng.manage_course.exception;

import com.xuecheng.framework.exception.ExceptionCache;
import com.xuecheng.framework.model.response.CommonCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice //控制器增强
public class CustomExceptionCatch extends ExceptionCache {

    static {
        builder.put(AccessDeniedException.class, CommonCode.UNAUTHORISE);
    }
}
