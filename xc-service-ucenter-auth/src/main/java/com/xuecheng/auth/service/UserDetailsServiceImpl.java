package com.xuecheng.auth.service;

import com.xuecheng.auth.client.UserClient;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ClientDetailsService clientDetailsService;
    @Autowired
    private UserClient userClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //没有认证统一采用httpbasic认证，httpbasic中存储了client_id和client_secret，开始认证client_id和client_secret
        if(authentication==null){
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);
            if(clientDetails!=null){
                //密码
                String clientSecret = clientDetails.getClientSecret();
                return new User(username,clientSecret,AuthorityUtils.commaSeparatedStringToAuthorityList(""));
            }
        }
        if (StringUtils.isEmpty(username)) {
            return null;
        }

        // 远程请求用户中心,根据账号查询用户信息
        XcUserExt userext = userClient.getUserext(username);
        if (userext == null){
            // 如果返回空,安全框架会认为用户不存在
            return null;
        }


        /*XcUserExt userext = new XcUserExt();
        userext.setUsername("itcast");
        userext.setPassword(new BCryptPasswordEncoder().encode("123"));*/
        // userext.setPermissions(new ArrayList<XcMenu>());  // 权限先用静态的

        // 从数据库取出正确密码（hash值）
        String password = userext.getPassword();

        //这里暂时使用静态密码
        // String password ="123";
        //用户权限，这里暂时使用静态数据，最终会从数据库读取

        // 从数据库获取权限,可能拥有多个权限,所以是一个列表
        List<XcMenu> permissions = userext.getPermissions();
        if (CollectionUtils.isEmpty(permissions)){
            // 如果权限列表为空,创建一个对象,防止空指针异常
            permissions = new ArrayList<>();
        }
        List<String> user_permission = new ArrayList<>();
        permissions.forEach(item-> user_permission.add(item.getCode()));

        // 使用静态的权限表示用户所拥有的权限
        // user_permission.add("course_get_baseinfo");  // 查询课程信息
        // user_permission.add("course_pic_list");  // 图片查询权限

        String user_permission_string  = StringUtils.join(user_permission.toArray(), ",");
        List<GrantedAuthority> list = AuthorityUtils.commaSeparatedStringToAuthorityList(user_permission_string);
        // 将用户名 密码 用户所用户的权限放入构造方法中,构造安全框架所要求的 UserJwt对象(这个对象是一个拓展对象,继承了安全框架的User对象)
        UserJwt userDetails = new UserJwt(username, password, list);

        userDetails.setId(userext.getId());
        userDetails.setUtype(userext.getUtype());//用户类型
        // 所属企业,把企业的id放入用户对象中
        userDetails.setCompanyId(userext.getCompanyId());
        userDetails.setName(userext.getName());//用户名称
        userDetails.setUserpic(userext.getUserpic());//用户头像

        return userDetails;
    }
}
