package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcMenu;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper  // 定义mapper接口
public interface XcMenuMapper {

    // 根据用户的id查询用户的权限
    public List<XcMenu> selectPermissionByUserId(String userId);
}
