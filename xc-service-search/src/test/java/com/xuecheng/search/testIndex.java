package com.xuecheng.search;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class testIndex {

    // 将高等级和低等级的客户端注入到此处
    @Autowired
    private RestHighLevelClient client; // 优先使用高等级的客户端,当高等级的客户端不满足需求,才使用低等级的客户端
    @Autowired
    private RestClient restClient;


    // 测试删除索引库
    @Test
    public void testDelDoc() throws IOException {
        // 删除索引对象
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("xc_source");

        // 操作索引的客户端
        IndicesClient indices = client.indices();
        // 执行删除索引
        DeleteIndexResponse delete = indices.delete(deleteIndexRequest);

        // 得到相应
        boolean acknowledged = delete.isAcknowledged();
        System.out.println(acknowledged);  // 返回true 表示删除成功

    }

    // 创建索引库
    @Test
    public void testCreateIndex() throws IOException {

        // 创建索引对象
        CreateIndexRequest xc_source = new CreateIndexRequest("xc_source");
        // 设置索引的参数  分片和副本的数量
        xc_source.settings(Settings.builder().put("number_of_shards",1).put("number_of_replicas",0));

        // 指定映射的内容
        String jsonStr = " {\n" +
                " \t\"properties\": {\n" +
                " \"name\": {\n" +
                " \"type\": \"text\",\n" +
                " \"analyzer\":\"ik_max_word\",\n" +
                " \"search_analyzer\":\"ik_smart\"\n" +
                " },\n" +
                " \"description\": {\n" +
                " \"type\": \"text\",\n" +
                " \"analyzer\":\"ik_max_word\",\n" +
                " \"search_analyzer\":\"ik_smart\"\n" +
                " },\n" +
                " \"studymodel\": {\n" +
                " \"type\": \"keyword\"\n" +
                " },\n" +
                " \"price\": {\n" +
                " \"type\": \"float\"\n" +
                " }\n" +
                " }\n" +
                "}";

        // 指定映射 指定一个无意义的名称 + json串 指定数据的类型是json
        xc_source.mapping("doc", jsonStr, XContentType.JSON);

        // 操作索引的客户端
        IndicesClient indices = client.indices();
        // 执行创建索引库
        CreateIndexResponse createIndexResponse = indices.create(xc_source);
        // 获取创建索引库之后的响应
        System.out.println(createIndexResponse.isShardsAcknowledged());

    }

    // 添加文档
    @Test
    public void testAddDoc() throws IOException {

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", "spring cloud实战");
        jsonMap.put("description", "本课程主要从四个章节进行讲解： 1.微服务架构入门 2.spring cloud 基础入门 3.实战Spring Boot 4.注册中心eureka。");
        jsonMap.put("studymodel", "201001");
        jsonMap.put("price", 5.6f);
        // 创建索引的创建对象
        IndexRequest indexRequest = new IndexRequest("xc_source","doc");
        // 指定索引文档内容
        indexRequest.source(jsonMap);
        // 通过client进行http的请求
        IndexResponse response = client.index(indexRequest);
        // 获取相应结果
        DocWriteResponse.Result result = response.getResult();
        System.out.println(result);  // CREATED
    }

    // 查询文档
    @Test
    public void getDoc() throws IOException {
        // 查询的请求对象
        GetRequest getRequest = new GetRequest(
                "xc_source",
                "doc",
                "a6j62m4Bl1UHSxFwdQN1");

        // 获取相应对象
        GetResponse getResponse = client.get(getRequest);
        boolean exists = getResponse.isExists();
        System.out.println(exists);  // true

        Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
        System.out.println(sourceAsMap); //
    }

    // 局部更新文档
    //更新文档
    @Test
    public void updateDoc() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("xc_source", "doc", "a6j62m4Bl1UHSxFwdQN1");
        Map<String, String> map = new HashMap<>();
        // 指定要更新的字段
        map.put("name", "spring cloud实战!!!");
        updateRequest.doc(map);
        UpdateResponse update = client.update(updateRequest);
        RestStatus status = update.status();
        System.out.println(status);  // OK
    }

    //根据id删除文档,只清空文档的内容,但是索引库的映射结构还是在的
    @Test
    public void testDelById() throws IOException {
        // 删除文档id
        String id = "a6j62m4Bl1UHSxFwdQN1";
        // 删除索引请求对象
        DeleteRequest deleteRequest = new DeleteRequest("xc_source","doc",id);
        // 响应对象
        DeleteResponse deleteResponse = client.delete(deleteRequest);
        // 获取响应结果
        DocWriteResponse.Result result = deleteResponse.getResult();
        System.out.println(result);  // DELETED
    }

}
