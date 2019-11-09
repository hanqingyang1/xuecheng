package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GridFsTest {

    @Autowired
    private GridFsTemplate gridFsTemplate;
    
    @Autowired
    private GridFSBucket gridFSBucket;

/*    @Test
    public void testStore() throws FileNotFoundException {
        File file = new File("G:/index_banner.ftl");
        FileInputStream inputStream = new FileInputStream(file);
        ObjectId objectId = gridFsTemplate.store(inputStream, "index_banner.ftl");
        System.out.println(objectId);//5d89431bccc032102cea799e
    }*/

    //存文件
    @Test
    public void testStore() throws FileNotFoundException {
        //定义file
        File file =new File("G:/course.ftl");
        //定义fileInputStream
        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectId objectId = gridFsTemplate.store(fileInputStream, "course.ftl");
        System.out.println(objectId);
    }


    @Test
    public void queryFile() throws Exception{
        GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("5d89431bccc032102cea799e")));
        gridFSBucket.openDownloadStream(file.getObjectId());
    }


}
