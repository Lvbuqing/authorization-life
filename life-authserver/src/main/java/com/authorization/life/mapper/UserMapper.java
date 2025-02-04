package com.authorization.life.mapper;

import java.util.List;

import com.authorization.life.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * 用户表 持久
 *
 * @author code@code.com 2022-02-21 20:23:16
 */
@Repository
public interface UserMapper extends BaseMapper<User> {

    List<User> page(User user);

}
