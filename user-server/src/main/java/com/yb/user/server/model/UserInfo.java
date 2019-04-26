package com.yb.user.server.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;
import java.io.Serializable;

/**
 * author biaoyang
 * Date: 2019/4/26 0026
 * Description: 用户信息类
 */
@Setter
@Getter
@ToString
@Document
public class UserInfo implements Serializable {
    private static final long serialVersionUID = 5681395472652334880L;

    private String id;
    //用户名
    private String username;
    //密码
    private String password;
    //角色
    private String[] roles;

}
