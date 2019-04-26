package com.yb.user.server.exception;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;


/**
 * author biaoyang
 * Date: 2019/4/8 0008
 * Description: 接口统一异常捕捉类
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public String constraintViolationExceptionHandler(ConstraintViolationException e, Model model) {
        log.error(e.getMessage(), e);
        final String message = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .reduce((s, s2) -> s + ", " + s2)
                .orElse("");
        //封装数据
        model.addAttribute("checkMessage", message);
        return "forward:/checkMessage";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException.class)
    public String exceptionHandler(RuntimeException e, Model model) {
        log.error(e.getMessage(), e);
        model.addAttribute("checkMessage", e.getMessage());
        return "forward:/checkMessage";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public String exceptionHandler(Exception e, Model model) {
        log.error(e.getMessage(), e);
        model.addAttribute("checkMessage", e.getMessage());
        return "forward:/checkMessage";
    }

}
