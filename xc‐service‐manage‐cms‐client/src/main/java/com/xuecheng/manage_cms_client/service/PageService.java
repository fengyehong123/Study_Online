package com.xuecheng.manage_cms_client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.Optional;

@Service
public class PageService {
    // 日志的记录对象
    private static final Logger LOGGER = LoggerFactory.getLogger(PageService.class);

    @Autowired
    private CmsPageRepository cmsPageRepository;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    // 保存html页面到服务器的物理路径
    public void savePageToServerPath(String pageId){

        // 根据pageId查询cmsPage
        CmsPage cmsPage = this.findCmsPageById(pageId);

        // 得到html的文件id,从cmsPage中获取htmlFileId的内容
        String htmlFileId = cmsPage.getHtmlFileId();

        // 从gridFS中查询html文件
        InputStream inputStream = this.getFileById(htmlFileId);
        if (inputStream == null){
            // 把出错信息记录到日志上
            LOGGER.error("getFileById InputStream is null,htmlFileId is {}",htmlFileId);
            return;
        }

        // 根据站的id得到站点的信息
        CmsSite cmsSite = this.findCmsSiteById(cmsPage.getSiteId());

        // 得到站点的物理路径
        String sitePhysicalPath = cmsSite.getSitePhysicalPath();

        // 得到页面的物理路径
        String pagePath = sitePhysicalPath + cmsPage.getPagePhysicalPath() + cmsPage.getPageName();
        System.out.println("!!!!!pagePath is" + pagePath);

        // 将html文件保存到服务器的物理路径上
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(pagePath));
            // 把输入流保存到输出流
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    // 根据页面的id查询页面的信息
    public CmsPage findCmsPageById(String pageId){
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (optional.isPresent()){
            CmsPage cmsPage = optional.get();

            return cmsPage;
        }
        return null;
    }

    // 根据htmlFileId从GridFS中查询文件的内容
    private InputStream getFileById(String fileId){
        // 根据文件的id查询文件的对象
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        // 打开下载流,根据文件对象获取下载流对象
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        // 定义GridFsResource
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);

        // 获取流对象
        try {
            InputStream inputStream = gridFsResource.getInputStream();

            return inputStream;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 根据站点的id查询站点的信息
    private CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optionalSite = cmsSiteRepository.findById(siteId);
        if (optionalSite.isPresent()){
            CmsSite cmsSite = optionalSite.get();

            return cmsSite;
        }

        return null;
    }


}
