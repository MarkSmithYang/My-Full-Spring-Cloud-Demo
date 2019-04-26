package com.yb.user.server.repository;


import com.yb.user.server.model.UserInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * author biaoyang
 * Date: 2019/4/26 0026
 * Description:
 */
public interface UserInfoRepository extends MongoRepository<UserInfo,String> {

    /**
     * 根据用户名查询用户信息--设定用户名唯一
     * @param username
     * @return
     */
    UserInfo findByUsername(String username);

}
