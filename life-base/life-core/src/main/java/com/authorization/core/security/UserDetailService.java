package com.authorization.core.security;

import com.authorization.core.entity.UserDetail;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserDetailService extends UserDetailsService {

    UserDetail createUserDetailByUser(UserDetails userDetails);

}
