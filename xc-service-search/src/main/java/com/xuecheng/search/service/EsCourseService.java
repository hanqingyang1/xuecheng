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
import java.util.*;

@Service
public class EsCourseService {

    @Value("${xuecheng.elasticsearch.course.index}")
    private String index;
    @Value("${xuecheng.elasticsearch.course.type}")
    private String type;
    @Value("${xuecheng.elasticsearch.media.index}")
    private String media_index;
    @Value("${xuecheng.elasticsearch.media.type}")
    private String media_type;
    @Value("${xuecheng.elasticsearch.course.source_field}")
    private String source_field;
    @Value("${xuecheng.elasticsearch.media.source_field}")
    private String media_source_field;
    @Autowired
    private RestHighLevelClient client;


    /**
     * 课程搜素
     * @param page
     * @param size
     * @param courseSearchParam
     * @return
     */
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {

        //创建搜索请求对象并设置索引库名和类型
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if(StringUtils.isNotBlank(courseSearchParam.getKeyword())) {
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(),
                    "name", "description", "teachplan");
            multiMatchQueryBuilder.minimumShouldMatch("70%").field("name", 10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        if(StringUtils.isNotBlank(courseSearchParam.getMt())){

            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        if(StringUtils.isNotBlank(courseSearchParam.getSt())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }
        if(StringUtils.isNotBlank(courseSearchParam.getGrade())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }

        searchSourceBuilder.query(boolQueryBuilder);
        if(page <= 0){
            page = 1;
        }
        if(size <= 0){
            size = 10;
        }
        int from = (page - 1)*size;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);
        String[] source_field_array = source_field.split(",");
        searchSourceBuilder.fetchSource(source_field_array,new String []{});
        searchRequest.source(searchSourceBuilder);

        QueryResult<CoursePub> queryResult = new QueryResult<>();
        List<CoursePub> coursePubs = new ArrayList<>(30);
         try {
            SearchResponse searchResponse = client.search(searchRequest);
             SearchHits hits = searchResponse.getHits();
             long totalHits = hits.getTotalHits();
             queryResult.setTotal(totalHits);
             SearchHit[] searchHits = hits.getHits();
             Arrays.stream(searchHits).forEach(s -> {
                 CoursePub coursePub = new CoursePub();
                 Map<String, Object> sourceAsMap = s.getSourceAsMap();
                 String name = (String) sourceAsMap.get("name");
                 String id = (String) sourceAsMap.get("id");
                 coursePub.setId(id);
                 Map<String, HighlightField> highlightFields = s.getHighlightFields();
                 if(highlightFields != null){
                     HighlightField highlightField = highlightFields.get("name");
                     if(highlightField != null){
                         Text[] fragments = highlightField.fragments();
                         StringBuffer stringBuffer = new StringBuffer();
                         for (Text text : fragments) {
                             stringBuffer.append(text);
                         }
                         name = stringBuffer.toString();

                     }
                 }
                 coursePub.setName(name);
                 //图片
                 String pic = (String) sourceAsMap.get("pic");
                 coursePub.setPic(pic);
                 //价格
                 Double price = null;
                 try {
                     if(sourceAsMap.get("price")!=null ){
                         price = (Double) sourceAsMap.get("price");
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 coursePub.setPrice(price);
                 Double price_old = null;
                 try {
                     if(sourceAsMap.get("price_old")!=null ){
                         price_old = (Double) sourceAsMap.get("price_old");
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 coursePub.setPrice_old(price_old);
                 coursePubs.add(coursePub);
             });
         } catch (IOException e) {
            e.printStackTrace();
        }
        queryResult.setList(coursePubs);
        QueryResponseResult<CoursePub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }

    /**
     * 根据课程Id搜素课程计划
     * @param id
     * @return
     */
    public Map<String, CoursePub> getall(String id) {

        //封装请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        //设置类型
        searchRequest.types(type);
        //窗前请求构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //使用termquery
        searchSourceBuilder.query(QueryBuilders.termQuery("id",id));

        searchRequest.source(searchSourceBuilder);
        //执行搜索
        try {
            SearchResponse searchResponse = client.search(searchRequest);
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            Map<String,CoursePub> map = new HashMap<>();
            for (SearchHit hit : searchHits) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                String courseId = (String) sourceAsMap.get("id");
                String name = (String) sourceAsMap.get("name");
                String grade = (String) sourceAsMap.get("grade");
                String charge = (String) sourceAsMap.get("charge");
                String pic = (String) sourceAsMap.get("pic");
                String description = (String) sourceAsMap.get("description");
                String teachplan = (String) sourceAsMap.get("teachplan");
                CoursePub coursePub = new CoursePub();
                coursePub.setId(courseId);
                coursePub.setName(name);
                coursePub.setPic(pic);
                coursePub.setGrade(grade);
                coursePub.setTeachplan(teachplan);
                coursePub.setDescription(description);
                map.put(courseId,coursePub);
            }

            return map;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanIds) {
        //创建请求对象
        SearchRequest searchRequest = new SearchRequest(media_index);
        //设置请求类型
        searchRequest.types(media_type);
        //创建搜索构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id",teachplanIds));
        String[] includes = media_source_field.split(",");
        searchSourceBuilder.fetchSource(includes,new String[]{});

        //封装请求对象
        searchRequest.source(searchSourceBuilder);

        List<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();
        long totalHits = 0;
        //执行搜索
        try {
            SearchResponse searchResponse = client.search(searchRequest);
            SearchHits hits = searchResponse.getHits();
            totalHits = hits.getTotalHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();

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

                teachplanMediaPubs.add(teachplanMediaPub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setList(teachplanMediaPubs);
        queryResult.setTotal(totalHits);
        QueryResponseResult<TeachplanMediaPub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);


        return queryResponseResult;
    }
}
