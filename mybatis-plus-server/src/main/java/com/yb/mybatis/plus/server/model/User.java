package com.yb.mybatis.plus.server.model;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

/**
 * @author biaoyang
 */
@Setter
@Getter
public class User implements Serializable {
    private static final long serialVersionUID = -5406845569890766326L;
    private Long id;
    private String name;
    private Integer age;
    private String email;

}

