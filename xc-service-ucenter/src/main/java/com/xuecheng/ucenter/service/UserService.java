package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private XcCompanyUserRepository xcCompanyUserRepository;
    @Autowired
    private XcUserRepository xcUserRepository;

    // 根据账号查询用户信息
    public XcUserExt getUserExt(String username){
        XcUser xcUser = this.findXcUserByUsername(username);
        if (xcUser == null){
            return null;
        }

        // 根据用户id查询用户所属公司的id
        String xcUserId = xcUser.getId();
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findByUserId(xcUserId);

        String companyId = null;
        // 如果用户为学生的话,学生没有对应的companyId,只有老师才会有
        if (xcCompanyUser!=null){
            companyId = xcCompanyUser.getCompanyId();
        }

        XcUserExt xcUserExt = new XcUserExt();
        // 因为new XcUserExt继承了 new XcUser 所以属性可以直接拷贝
        BeanUtils.copyProperties(xcUser, xcUserExt);
        xcUserExt.setCompanyId(companyId);

        return xcUserExt;

    }

    // 根据账号查询XcUser信息
    public XcUser findXcUserByUsername(String username){
        XcUser byUsername = xcUserRepository.findByUsername(username);
        return byUsername;
    }

}
