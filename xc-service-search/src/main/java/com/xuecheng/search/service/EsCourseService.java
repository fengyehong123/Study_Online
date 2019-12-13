package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EsCourseService {

    // 从配置文件中读取数据
    @Value("${xuecheng.course.index}")
    private String index;
    @Value("${xuecheng.course.type}")
    private String type;
    @Value("${xuecheng.course.source_field}")
    private String source_field;
    @Value("${xuecheng.media.index}")
    private String mediaIndex;
    @Value("${xuecheng.media.type}")
    private String mediaType;
    @Value("${xuecheng.media.source_field}")
    private String mediaSourceField;
    // 注入搜索用的客户端
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    // 进行搜索
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {

        if (courseSearchParam == null){
            courseSearchParam = new CourseSearchParam();
        }

        // 创建搜索的请求对象(指定索引库名称)
        SearchRequest searchRequest = new SearchRequest(index);
        // 设置搜索类型
        searchRequest.types(type);
        // 构建搜索的条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        String[] split = source_field.split(",");  // 将字符串用 , 分隔,组成数组
        // 指定要过滤的原字段(有些字段显示,有些字段不显示)
        searchSourceBuilder.fetchSource(split,new String[]{});


        // 创建一个布尔查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 添加搜索条件
        // 根据关键字来搜索
        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())){
            // 添加关键字搜索要搜索的字段 为name字段提高权重
            MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name", "description", "teachplan")
                    .minimumShouldMatch("70%").field("name", 10);
            // 将关键字查询对象添加到bool查询对象中
            boolQueryBuilder.must(matchQueryBuilder);
        }

        // 根据一级分类来进行搜索(精确匹配,不会对搜索的条件进行分词)
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        // 根据二级分类
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }
        // 根据难度等级
        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }

        // 分页参数
        if(page<=0){
            page = 1;
        }
        if(size<=0){
            size = 20;
        }
        int start = (page-1)*size;  // 计算起始下标
        searchSourceBuilder.from(start);
        searchSourceBuilder.size(size);

        // 设置boolQueryBuilder到searchSourceBuilder中
        searchSourceBuilder.query(boolQueryBuilder);

        // 高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");  //起始标签
        highlightBuilder.postTags("</font>");  // 结束标签
        // 设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);

        // 创建一个响应对象
        QueryResult<CoursePub> queryResult = new QueryResult();
        ArrayList<CoursePub> list = new ArrayList<>();

        // 指行搜索
        try {
            // 得到搜索后的对象
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            // 获取响应结果
            SearchHits hits = searchResponse.getHits();
            // 获取匹配的总记录数
            long totalHits = hits.getTotalHits();
            queryResult.setTotal(totalHits);

            // 获取到匹配度高的结果
            SearchHit[] hitsHits = hits.getHits();
            for (SearchHit hitsHit : hitsHits) {
                CoursePub coursePub = new CoursePub();

                // 获取源文档
                Map<String, Object> sourceAsMap = hitsHit.getSourceAsMap();
                // 取出id
                String id = (String) sourceAsMap.get("id");
                coursePub.setId(id);

                // 组装前端要展示的数据
                String name = (String) sourceAsMap.get("name");

                // 取出所有高亮字段的对象
                Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
                if(highlightFields!=null){
                    // 获取name字段的高亮对象
                    HighlightField nameField = highlightFields.get("name");
                    if(nameField!=null){
                        //取出name字段对应的高亮数据
                        Text[] fragments = nameField.getFragments();
                        StringBuffer stringBuffer = new StringBuffer();
                        for (Text str : fragments) {
                            stringBuffer.append(str.string());
                        }

                        name = stringBuffer.toString();
                    }
                }

                coursePub.setName(name);

                // 图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);

                // 新价格
                Double price = null;
                try {
                    if(sourceAsMap.get("price")!=null ){
                        price = (Double)sourceAsMap.get("price");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice(price);

                // 旧价格
                Double price_old = null;
                try {
                    if(sourceAsMap.get("price_old")!=null ){
                        price_old = (Double) sourceAsMap.get("price_old");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice_old(price_old);

                // 把得到的对象依次添加到列表中
                list.add(coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 把list添加到响应对象中
        queryResult.setList(list);

        QueryResponseResult<CoursePub> coursePubQueryResponseResult = new QueryResponseResult<CoursePub>(CommonCode.SUCCESS,queryResult);

        return coursePubQueryResponseResult;
    }

    // 使用ES的客户端向ES索引库请求查询索引信息
    public Map<String, CoursePub> getAll(String id) {

        // 定义一个搜索的请求对象(指定索引库的名称)
        SearchRequest searchRequest = new SearchRequest(index);
        // 指定type
        searchRequest.types(type);

        // 设置对源文档的搜索方法
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("id",id));
        /*不用过滤字段,我们要取出所有的字段
        searchSourceBuilder.fetchSource(, )*/

        // 将搜索方法的对象添加到搜索的请求对象中
        searchRequest.source(searchSourceBuilder);

        Map<String, CoursePub> map = new HashMap<>();
        try {
            // 调用ES搜索的客户端,进行搜索请求
            SearchResponse search = restHighLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            // 取出所有匹配的记录
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                // 最终要返回的课程信息对象
                CoursePub coursePub = new CoursePub();
                // 得到源文档的内容
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                // 获取出课程Id
                String courseId = (String) sourceAsMap.get("id");
                coursePub.setId(courseId);
                String name = (String) sourceAsMap.get("name");
                coursePub.setName(name);
                String grade = (String) sourceAsMap.get("grade");
                coursePub.setGrade(grade);
                String charge = (String) sourceAsMap.get("charge");
                coursePub.setCharge(charge);
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                String description = (String) sourceAsMap.get("description");
                coursePub.setDescription(description);
                String teachplan = (String) sourceAsMap.get("teachplan");
                coursePub.setTeachplan(teachplan);

                map.put(courseId,coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    // 根据多个课程计划id查询课程媒资信息
    public QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanIds) {

        // 定义一个搜索的请求对象(指定索引库的名称)
        SearchRequest searchRequest = new SearchRequest(mediaIndex);
        // 指定type
        searchRequest.types(mediaType);

        // 设置对源文档的搜索方法
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 根据数组来进行查询根据多个课程id的数组来进行查询
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id",teachplanIds));
        // 过滤原字段,把字符串转换为数组
        String[] split = mediaSourceField.split(",");
        // 参数1:包括的字段,参数2:不包括的字段
        searchSourceBuilder.fetchSource(split,new String[]{});

        // 将搜索方法的对象添加到搜索的请求对象中
        searchRequest.source(searchSourceBuilder);

        List<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();
        long total = 0;
        // 使用Es客户端进行搜索
        try {
            SearchResponse search = restHighLevelClient.search(searchRequest);
            // 得到匹配的结果
            SearchHits hits = search.getHits();
            total = hits.getTotalHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit searchHit : searchHits) {
                TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
                // 获取源文档
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                //取出课程计划媒资信息
                String courseid = (String) sourceAsMap.get("courseid");
                String media_id = (String) sourceAsMap.get("media_id");
                String media_url = (String) sourceAsMap.get("media_url");
                String teachplan_id = (String) sourceAsMap.get("teachplan_id");
                String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");
                teachplanMediaPub.setCourseId(courseid);
                teachplanMediaPub.setMediaUrl(media_url);
                teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);
                teachplanMediaPub.setMediaId(media_id);
                teachplanMediaPub.setTeachplanId(teachplan_id);

                // 添加到列表中
                teachplanMediaPubs.add(teachplanMediaPub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        QueryResult<TeachplanMediaPub> pubQueryResult = new QueryResult<>();
        pubQueryResult.setTotal(total);
        pubQueryResult.setList(teachplanMediaPubs);
        QueryResponseResult<TeachplanMediaPub> responseResult = new QueryResponseResult<TeachplanMediaPub>(CommonCode.SUCCESS,pubQueryResult);

        return responseResult;
    }
}
