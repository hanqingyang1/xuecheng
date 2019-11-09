package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CmsPageRepository extends MongoRepository<CmsPage,String> {
    //根据页面名称查询
    CmsPage findByPageName(String pageName);
    //根绝站点id,站点名，webpath查询数据是否存在
    CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName,String siteId,String webPath);
}
