package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.course.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

// 页面查询的接口
@Api(value="cms页面管理接口",description = "cms页面管理接口，提供页面的增、删、改、查")
public interface CmsPageControllerApi {

    // 当前页码 每页记录数 查询条件
    // aramType="path" 参数来源的途径 通过url中获取
    @ApiOperation("分页查询页面列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name="page",value = "页码",required=true,paramType="path",dataType="int"),
            @ApiImplicitParam(name="size",value = "每页记录数",required=true,paramType="path",dataType="int")
    })
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);


    // 查询所有的站点
    @ApiOperation("查询所有的站点")
    public QueryResponseResult findSite();

    // 新增页面
    @ApiOperation("新增页面")
    public CmsPageResult add(CmsPage cmsPage);


    // 根据页面的id查询页面的信息
    @ApiOperation("根据id查询页面信息")
    public CmsPage findById(String id);

    // 修改页面
    @ApiOperation("修改页面")
    public CmsPageResult edit(String id, CmsPage cmsPage);


    // 删除页面
    @ApiOperation("删除页面")
    public ResponseResult delete(String id);

    // 页面发布的接口
    @ApiOperation("页面发布")
    public ResponseResult post(String pageId);

    // 有了页面的话,就更新,没有的话,就添加页面
    @ApiOperation("保存页面")
    public CmsPageResult save(CmsPage cmsPage);

    @ApiOperation("一键发布页面")
    public CmsPostPageResult postPageQuick(CmsPage cmsPage);

}
