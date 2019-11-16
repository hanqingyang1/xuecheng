package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.TermQuery;
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

import javax.swing.text.Highlighter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class EsCourseService {

    @Value("${xuecheng.elasticsearch.course.index}")
    private String index;
    @Value("${xuecheng.elasticsearch.course.type}")
    private String type;
    @Value("${xuecheng.elasticsearch.course.source_field}")
    private String source_field;
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
}
