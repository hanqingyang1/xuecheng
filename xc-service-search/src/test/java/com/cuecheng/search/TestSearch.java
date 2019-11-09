package com.cuecheng.search;

import com.xuecheng.search.config.ElasticsearchConfig;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@SpringBootTest(classes = ElasticsearchConfig.class)
@RunWith(SpringRunner.class)
public class TestSearch {


    @Autowired
    RestHighLevelClient client;
    @Autowired
    RestClient restClient;

    @Test
    public void testSearchAll() throws IOException {
        //创建请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //设置类型
        searchRequest.types("doc");
        //创建请求构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //添加请求方式
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //添加搜索过滤
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //向请求对象中添加搜素源
        SearchRequest request = searchRequest.source(searchSourceBuilder);
        //执行搜素
        SearchResponse searchResponse = client.search(request);
        //获取搜索结构
        SearchHits hits = searchResponse.getHits();
        //获取总记录数
        long totalHits = hits.getTotalHits();
        //获取匹配度高的结果
        SearchHit[] searchHits = hits.getHits();

        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }

    /**
     * 分页查询
     * @throws IOException
     */
    @Test
    public void testSearchPage() throws IOException {
        //创建请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //设置类型
        searchRequest.types("doc");
        //创建请求构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        int page = 1;
        int size = 1;
        //封装分页参数
        int from = (page -1)*size;

        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);

        //添加请求方式
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //添加搜索过滤
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //向请求对象中添加搜素源
        SearchRequest request = searchRequest.source(searchSourceBuilder);
        //执行搜素
        SearchResponse searchResponse = client.search(request);
        //获取搜索结构
        SearchHits hits = searchResponse.getHits();
        //获取总记录数
        long totalHits = hits.getTotalHits();
        //获取匹配度高的结果
        SearchHit[] searchHits = hits.getHits();

        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }

    /**
     * 词条匹配查询
     * @throws IOException
     */
    @Test
    public void testTermQuery() throws IOException {
       //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //设置类型
       searchRequest.types("doc");
       //创建搜索构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //指定搜素leixing
        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring"));
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});

        //封装请求
        SearchRequest request = searchRequest.source(searchSourceBuilder);
        //执行请求
        SearchResponse searchResponse = client.search(request);

        SearchHits hits = searchResponse.getHits();
        //获取总记录数
        long totalHits = hits.getTotalHits();
        //获取匹配度高的结果
        SearchHit[] searchHits = hits.getHits();

        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }


    /**
     * 根据id查询
     * @throws IOException
     */
    @Test
    public void testTermQueryByIds() throws IOException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");

        //设置类型
        searchRequest.types("doc");
        //创建搜索构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String[] ids = {"1", "3"};
        //指定搜素leixing 注意是termsQuery 不同于termQuery
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id",ids));
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});

        //封装请求
        SearchRequest request = searchRequest.source(searchSourceBuilder);
        //执行请求
        SearchResponse searchResponse = client.search(request);
        SearchHits hits = searchResponse.getHits();
        //获取总记录数
        long totalHits = hits.getTotalHits();
        //获取匹配度高的结果
        SearchHit[] searchHits = hits.getHits();

        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String sourceAsString = hit.getSourceAsString();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }

    /**
     * MatchQuery
     * @throws IOException
     */
    @Test
    public void testMatchQuery() throws IOException {
        //创建请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //设置类型
        searchRequest.types("doc");
        //创建查询构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置查询类型
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("spring css","name","description")
                .minimumShouldMatch("50%")
                .field("name",10));
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //封装请求对象
        SearchRequest request = searchRequest.source(searchSourceBuilder);
        //执行搜素
        SearchResponse searchResponse = client.search(request);
        //获取搜素内容
        SearchHits hits = searchResponse.getHits();
        //获取总记录数
        long totalHits = hits.getTotalHits();
        //获取命中率高的记录
        SearchHit[] searchHits = hits.getHits();
    }

    @Test
    public void testBoolQuery() throws IOException {
        //创建请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //设置类型
        searchRequest.types("doc");
        //创建查询构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //封装multiMatchQuery对象
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);
        //封装TermQuery对象
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("studymodel", "201001");
        //创建boolQuery查询构建器
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        boolQueryBuilder.must(termQueryBuilder);

        searchSourceBuilder.query(boolQueryBuilder);

        //封装请求对象
        SearchRequest request = searchRequest.source(searchSourceBuilder);
        //执行请求
        SearchResponse searchResponse = client.search(request);
        SearchHits hits = searchResponse.getHits();
        //获取总记录数
        long totalHits = hits.getTotalHits();
        //获取匹配度较高的记录
        SearchHit[] searchHits = hits.getHits();

        Arrays.stream(searchHits).forEach(h -> {
            Map<String, Object> sourceAsMap = h.getSourceAsMap();
            String sourceAsString = h.getSourceAsString();

        });



    }


    @Test
    public void testFilterQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%").field("name", 10);
        boolQueryBuilder.must(multiMatchQueryBuilder);
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel","201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(80).lte(100));

        searchSourceBuilder.query(boolQueryBuilder);
        SearchRequest request = searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(request);

        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();
        SearchHit[] searchHits = hits.getHits();
        Arrays.stream(searchHits)
                .forEach(h -> {
                    Map<String, Object> sourceAsMap = h.getSourceAsMap();
                    String sourceAsString = h.getSourceAsString();
                });

    }

}
