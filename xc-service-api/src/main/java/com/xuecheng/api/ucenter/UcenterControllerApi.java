package com.xuecheng.api.ucenter;

import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "用户中心",description = "用户中心管理")
public interface UcenterControllerApi {

    // 返回的类型是要包含用户的基本信息和用户的图片公司等信息,所以一定是一个拓展类型
    @ApiOperation("根据用户账号来查询用户信息")
    public XcUserExt getUserext(String username);
}