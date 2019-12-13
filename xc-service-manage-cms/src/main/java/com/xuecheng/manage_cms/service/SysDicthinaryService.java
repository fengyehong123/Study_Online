package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.manage_cms.dao.SysDictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysDicthinaryService {

    @Autowired
    private SysDictionaryRepository sysDictionaryRepository;

    // 新增页面查询数据字典
    public SysDictionary getSysDictionaryByType(String type) {

        SysDictionary sysDictionary = sysDictionaryRepository.findByDType(type);

        if (sysDictionary == null){
            // 如果查询不到,说明参数输入错误,没有这个类型
            ExceptionCast.cast(CommonCode.INCALID_PARAM);
        }

        return sysDictionary;
    }
}
