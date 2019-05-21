package com.yb.mybatis.plus.server;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yb.mybatis.plus.server.mapper.MyBatisPlusServerMapper;
import com.yb.mybatis.plus.server.model.User;
import lombok.AllArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
//@AllArgsConstructor
public class MybatisPlusServerApplicationTests {

    @Autowired
    private MyBatisPlusServerMapper myBatisPlusServerMapper;

    @Test
    public void contextLoads() {
        List<User> users = myBatisPlusServerMapper.selectList(null);
        Integer integer = myBatisPlusServerMapper.selectCount(null);
        IPage<User> page = new Page<>();
        page.setSize(2);
        IPage<User> page1 = myBatisPlusServerMapper.selectPage(page, null);
        User user = new User();
        user.setId(111L);
        user.setName("jack");
        user.setAge(19);
        user.setEmail("jack@163.com");
        int insert = myBatisPlusServerMapper.insert(user);
        System.err.println(integer);
    }

}
