package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

// 接口的名称格式 对应的数据库的民称 + Repository
// MongoRepository<CmsPage,String>  模型类和主键的类型
public interface CmsPageRepository extends MongoRepository<CmsPage,String>{

    // 根据页面名称来查询
    CmsPage findByPageName(String pageName);

    // 根据页面名称,站点id,页面的webpath去cms_page集合查询,判断唯一性
    CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName,String siteId,String pageWebPath);
}
