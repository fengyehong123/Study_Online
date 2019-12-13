package com.xuecheng.search;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class testSearch {

    @Autowired
    private RestHighLevelClient client; // 优先使用高等级的客户端,当高等级的客户端不满足需求,才使用低等级的客户端
    @Autowired
    private RestClient restClient;

    // 搜索全部的记录(无搜索条件)
    @Test
    public void testSearchAll() throws IOException, ParseException {
        // 搜索的请求对象 需要指定搜索的库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");

        // 构建搜索源的对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 设置搜索方式(搜索全部)
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // 设置源字段过滤 参数1:结果集中包含的字段 参数2:结果集中不包含的字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});

        // 向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        // 进行搜索,向ES发起Http请求
        SearchResponse searchResponse = client.search(searchRequest);

        // 设置一个日期格式化的对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 获取搜索结果
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();  // 匹配到的总记录数
        SearchHit[] searchHits = hits.getHits();  // 得到匹配度高的文档数组
        for (SearchHit hit : searchHits) {
            // 获取到文档的主键
            String id = hit.getId();
            // 获取到源文档内容 ==> 把源文档内容(原先插入的数据)转换为source显示
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 获取源文档中的name对应的value
            String name = (String)sourceAsMap.get("name");
            // 由于前面设置了源文档字段的过滤,因此description对应的value无法获取
            String description = (String) sourceAsMap.get("description");
            // 获取日期
            Date date = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            // 获取价格
            Double price = (Double) sourceAsMap.get("price");
            System.out.println(id);
            System.out.println(name);
            System.out.println(description);
            System.out.println(date);
            System.out.println(price);
        }

    }

    // 进行分页查询(无搜索条件,但有分页条件)
    @Test
    public void testSearchByPage() throws IOException, ParseException {
        // 搜索的请求对象 需要指定搜索的库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");

        // 构建搜索源的对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 设置分页参数
        searchSourceBuilder.from(0);  // 起始的记录下标,从0开始 (page-1)*size
        searchSourceBuilder.size(1);  // 每页显示的记录数
        // 设置搜索方式(搜索全部)
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // 设置源字段过滤 参数1:结果集中包含的字段 参数2:结果集中不包含的字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});

        // 向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        // 进行搜索,向ES发起Http请求
        SearchResponse searchResponse = client.search(searchRequest);

        // 设置一个日期格式化的对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 获取搜索结果
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();  // 匹配到的总记录数
        SearchHit[] searchHits = hits.getHits();  // 得到匹配度高的文档数组
        for (SearchHit hit : searchHits) {
            // 获取到文档的主键
            String id = hit.getId();
            // 获取到源文档内容 ==> 把源文档内容(原先插入的数据)转换为source显示
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 获取源文档中的name对应的value
            String name = (String)sourceAsMap.get("name");
            // 由于前面设置了源文档字段的过滤,因此description对应的value无法获取
            String description = (String) sourceAsMap.get("description");
            // 获取日期
            Date date = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(id);
            System.out.println(name);
            System.out.println(description);
            System.out.println(date);
        }

    }


    // 进行精确匹配查询(不会对查询条件进行分词)
    @Test
    public void testSearchTermQuery() throws IOException, ParseException {
        // 搜索的请求对象 需要指定搜索的库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");

        // 构建搜索源的对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 设置搜索方式(精确匹配,不会分词) 指定要搜索的字段和搜索的内容
        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring"));

        // 设置源字段过滤 参数1:结果集中包含的字段 参数2:结果集中不包含的字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});

        // 向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        // 进行搜索,向ES发起Http请求
        SearchResponse searchResponse = client.search(searchRequest);

        // 设置一个日期格式化的对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 获取搜索结果
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();  // 匹配到的总记录数
        SearchHit[] searchHits = hits.getHits();  // 得到匹配度高的文档数组
        for (SearchHit hit : searchHits) {
            // 获取到文档的主键
            String id = hit.getId();
            // 获取到源文档内容 ==> 把源文档内容(原先插入的数据)转换为source显示
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 获取源文档中的name对应的value
            String name = (String)sourceAsMap.get("name");
            // 由于前面设置了源文档字段的过滤,因此description对应的value无法获取
            String description = (String) sourceAsMap.get("description");
            // 获取日期
            Date date = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(id);
            System.out.println(name);
            System.out.println(description);
        }

    }

    // 根据id进行精确匹配查询 !!!!!!!!!!!!!!!!!! 注意!!!!! QueryBuilders.termsQuery("_id",ids) termsQuery 多了一个s,如果不用带s的去查询,是查询不到的
    @Test
    public void testSearchTermQueryById() throws IOException, ParseException {
        // 搜索的请求对象 需要指定搜索的库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");

        // 构建搜索源的对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 根据id查询
        // 定义id
        String[] ids = new String[]{"1","2","100"};
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id",ids));

        // 设置源字段过滤 参数1:结果集中包含的字段 参数2:结果集中不包含的字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});

        // 向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        // 进行搜索,向ES发起Http请求
        SearchResponse searchResponse = client.search(searchRequest);

        // 设置一个日期格式化的对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 获取搜索结果
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();  // 匹配到的总记录数
        SearchHit[] searchHits = hits.getHits();  // 得到匹配度高的文档数组
        for (SearchHit hit : searchHits) {
            // 获取到文档的主键
            String id = hit.getId();
            // 获取到源文档内容 ==> 把源文档内容(原先插入的数据)转换为source显示
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 获取源文档中的name对应的value
            String name = (String)sourceAsMap.get("name");
            // 由于前面设置了源文档字段的过滤,因此description对应的value无法获取
            String description = (String) sourceAsMap.get("description");
            // 获取日期
            Date date = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(id);
            System.out.println(description);
        }

    }


    // 全文检索(对检索条件分词.检索特定的域)
    @Test
    public void testMatchQuery() throws IOException, ParseException {
        // 搜索的请求对象 需要指定搜索的库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");

        // 构建搜索源的对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 设置搜索方式 全文检索(对检索条件分词) 对description字段进行检索 要求分词展占比为80%
        searchSourceBuilder.query(QueryBuilders.matchQuery("description", "spring开发框架").minimumShouldMatch("80%"));

        // 设置源字段过滤 参数1:结果集中包含的字段 参数2:结果集中不包含的字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp","description"}, new String[]{});

        // 向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        // 进行搜索,向ES发起Http请求
        SearchResponse searchResponse = client.search(searchRequest);

        // 设置一个日期格式化的对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 获取搜索结果
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();  // 匹配到的总记录数
        SearchHit[] searchHits = hits.getHits();  // 得到匹配度高的文档数组
        for (SearchHit hit : searchHits) {
            // 获取到文档的主键
            String id = hit.getId();
            // 获取到源文档内容 ==> 把源文档内容(原先插入的数据)转换为source显示
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 获取源文档中的name对应的value
            String name = (String)sourceAsMap.get("name");
            // 由于前面设置了源文档字段的过滤,因此description对应的value无法获取
            String description = (String) sourceAsMap.get("description");
            // 获取日期
            Date date = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(description);
            System.out.println("123");
        }

    }

    // MultiMatchQuery(可以搜索多个域,例如name域和description域,我们可以提升域的权重)
    @Test
    public void testMultiMatchQuery() throws IOException, ParseException {
        // 搜索的请求对象 需要指定搜索的库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");

        // 构建搜索源的对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 设置搜索方式 MultiMatchQuery(可以搜索多个域,例如name域和description域)
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("spring css", "name","description").minimumShouldMatch("50%")
                                    .field("name",10));  // 提升name字段的权重

        // 设置源字段过滤 参数1:结果集中包含的字段 参数2:结果集中不包含的字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp","description"}, new String[]{});

        // 向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        // 进行搜索,向ES发起Http请求
        SearchResponse searchResponse = client.search(searchRequest);

        // 设置一个日期格式化的对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 获取搜索结果
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();  // 匹配到的总记录数
        SearchHit[] searchHits = hits.getHits();  // 得到匹配度高的文档数组
        for (SearchHit hit : searchHits) {
            // 获取到文档的主键
            String id = hit.getId();
            // 获取到源文档内容 ==> 把源文档内容(原先插入的数据)转换为source显示
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 获取源文档中的name对应的value
            String name = (String)sourceAsMap.get("name");
            // 由于前面设置了源文档字段的过滤,因此description对应的value无法获取
            String description = (String) sourceAsMap.get("description");
            // 获取日期
            Date date = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(description);
        }

    }

    // BoolQuery(可以包含多个查询(多个字段查询+某个字段精确匹配))
    @Test
    public void testBoolQuery() throws IOException, ParseException {
        // 搜索的请求对象 需要指定搜索的库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");

        // 构建搜索源的对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 1.定义multiMatchQuery的检索(多域检索)
        MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery("spring css", "name", "description").minimumShouldMatch("50%").field("name", 10);
        // 2.定义TermQueryBuilder精确匹配
        TermQueryBuilder termQuery = QueryBuilders.termQuery("studymodel", "201001");
        // 3. 定义一个BoolQuery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 通过BoolQueryBuilder对象,将多域检索和精确匹配结合起来
        boolQueryBuilder.must(multiMatchQuery);
        boolQueryBuilder.must(termQuery);

        // 进入布尔Query检错(检索中必须包含两个条件)
        searchSourceBuilder.query(boolQueryBuilder);

        // 设置源字段过滤 参数1:结果集中包含的字段 参数2:结果集中不包含的字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp","description"}, new String[]{});

        // 向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        // 进行搜索,向ES发起Http请求
        SearchResponse searchResponse = client.search(searchRequest);

        // 设置一个日期格式化的对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 获取搜索结果
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();  // 匹配到的总记录数
        SearchHit[] searchHits = hits.getHits();  // 得到匹配度高的文档数组
        for (SearchHit hit : searchHits) {
            // 获取到文档的主键
            String id = hit.getId();
            // 获取到源文档内容 ==> 把源文档内容(原先插入的数据)转换为source显示
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 获取源文档中的name对应的value
            String name = (String)sourceAsMap.get("name");
            // 由于前面设置了源文档字段的过滤,因此description对应的value无法获取
            String description = (String) sourceAsMap.get("description");
            // 获取日期
            Date date = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(description);
            System.out.println();
        }

    }

    // 布尔查询+过滤器 这种查询效率更高
    @Test
    public void testFilterQuery() throws IOException, ParseException {
        // 搜索的请求对象 需要指定搜索的库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");

        // 构建搜索源的对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 1.定义multiMatchQuery的检索(多域检索)
        MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery("spring css", "name", "description").minimumShouldMatch("50%").field("name", 10);

        // 3. 定义一个BoolQuery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 布尔查询添加条件1
        boolQueryBuilder.must(multiMatchQuery);
        // 布尔查询添加过滤器1(对精确查询的结果进行过滤,从而避免了计算得分,提高了效率)
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel","201001"));
        // 布尔查询添加过滤器2 只要价格在此范围内,就可以搜索出来
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100));

        // 添加布尔查询(包含多个域的查询和过滤器)
        searchSourceBuilder.query(boolQueryBuilder);

        // 设置源字段过滤 参数1:结果集中包含的字段 参数2:结果集中不包含的字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp","description"}, new String[]{});

        // 向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        // 进行搜索,向ES发起Http请求
        SearchResponse searchResponse = client.search(searchRequest);

        // 设置一个日期格式化的对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 获取搜索结果
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();  // 匹配到的总记录数
        SearchHit[] searchHits = hits.getHits();  // 得到匹配度高的文档数组
        for (SearchHit hit : searchHits) {
            // 获取到文档的主键
            String id = hit.getId();
            // 获取到源文档内容 ==> 把源文档内容(原先插入的数据)转换为source显示
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 获取源文档中的name对应的value
            String name = (String)sourceAsMap.get("name");
            // 由于前面设置了源文档字段的过滤,因此description对应的value无法获取
            String description = (String) sourceAsMap.get("description");
            // 获取日期
            Date date = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(description);
            System.out.println("---");
        }

    }

    // 排序
    @Test
    public void testSort() throws IOException, ParseException {
        // 搜索的请求对象 需要指定搜索的库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");

        // 构建搜索源的对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 3. 定义一个BoolQuery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 过滤查询
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));

        // 添加布尔查询(包含多个域的查询和过滤器)
        searchSourceBuilder.query(boolQueryBuilder);

        // 添加排序,指定排序的字段
        searchSourceBuilder.sort("studymodel", SortOrder.DESC);  // 根据studymodel降序
        searchSourceBuilder.sort("price", SortOrder.ASC);  // 根据价格升序



        // 设置源字段过滤 参数1:结果集中包含的字段 参数2:结果集中不包含的字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp","description"}, new String[]{});

        // 向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        // 进行搜索,向ES发起Http请求
        SearchResponse searchResponse = client.search(searchRequest);

        // 设置一个日期格式化的对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 获取搜索结果
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();  // 匹配到的总记录数
        SearchHit[] searchHits = hits.getHits();  // 得到匹配度高的文档数组
        for (SearchHit hit : searchHits) {
            // 获取到文档的主键
            String id = hit.getId();
            // 获取到源文档内容 ==> 把源文档内容(原先插入的数据)转换为source显示
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 获取源文档中的name对应的value
            String name = (String)sourceAsMap.get("name");
            // 由于前面设置了源文档字段的过滤,因此description对应的value无法获取
            String description = (String) sourceAsMap.get("description");
            // 获取日期
            Date date = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println("---");
            System.out.println(description);
        }

    }

    // 搜索结果进行高亮(对检索到的结果上添加标签,我们通过css给标签添加样式)
    @Test
    public void testHighLight() throws IOException, ParseException {
        // 搜索的请求对象 需要指定搜索的库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");

        // 构建搜索源的对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 1.定义multiMatchQuery的检索(多域检索)
        MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery("开发框架", "name", "description").minimumShouldMatch("50%").field("name", 10);

        // 3. 定义一个BoolQuery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 布尔查询添加条件1
        boolQueryBuilder.must(multiMatchQuery);
        // 布尔查询添加过滤器 只要价格在此范围内,就可以搜索出来
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));

        // 添加布尔查询(包含多个域的查询和过滤器)
        searchSourceBuilder.query(boolQueryBuilder);

        // 设置源字段过滤 参数1:结果集中包含的字段 参数2:结果集中不包含的字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp","description"}, new String[]{});

        // 设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<tag>");  // 定义高亮前面的标签
        highlightBuilder.postTags("</tag>");  // 定义高亮后面的标签
        // 添加要高亮的字段 对name字段进行高亮
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        // 向搜索源对象中添加高亮对象
        searchSourceBuilder.highlighter(highlightBuilder);


        // 向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        // 进行搜索,向ES发起Http请求
        SearchResponse searchResponse = client.search(searchRequest);

        // 设置一个日期格式化的对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 获取搜索结果
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();  // 匹配到的总记录数
        SearchHit[] searchHits = hits.getHits();  // 得到匹配度高的文档数组
        for (SearchHit hit : searchHits) {
            // 获取到文档的主键
            String id = hit.getId();
            // 获取到源文档内容 ==> 把源文档内容(原先插入的数据)转换为source显示
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 源文档的name字段的内容
            String name = (String)sourceAsMap.get("name");


            // 取出高亮后字段对象
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields != null){
                // 取出name高亮后字段
                HighlightField highlightNameField = highlightFields.get("name");
                if (highlightNameField!=null){

                    // 获取到的是一段段被高亮的内容数组,我们需要把这些内容拼接起来
                    Text[] fragments = highlightNameField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text fragment : fragments) {
                        // 开始拼接字符串
                        stringBuffer.append(fragment);
                    }
                    // 对源文档的name字段所对应的内容重新赋值
                    name = stringBuffer.toString();
                }
            }

            // 由于前面设置了源文档字段的过滤,因此description对应的value无法获取
            String description = (String) sourceAsMap.get("description");
            // 获取日期
            Date date = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(description);
            System.out.println("---");
            System.out.println(name);
        }

    }



}
