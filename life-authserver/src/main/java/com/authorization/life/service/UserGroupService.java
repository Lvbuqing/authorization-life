package com.authorization.life.service;

import com.authorization.life.entity.UserGroup;

import java.util.List;

/**
 * 用户组表
 *
 * @author code@code.com
 * @date 2022-02-21 20:24:00
 */
public interface UserGroupService {

    /**
     * 查找该用户所属的用户组
     */
    List<UserGroup> selectByUserId(Long userId);

}
