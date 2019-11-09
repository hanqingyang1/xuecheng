package com.xuecheng.test.fastdfs;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDfs {

    @Test
    public void upload_file(){

        try {
            //初始化配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义TrackerClient用于请求TrackerServer
            TrackerClient trackerClient = new TrackerClient();
            //链接TrackerServer
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取storage
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            //创建storageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);

            String filePath = "F:/BugReport.txt";
            
            //上传文件
            String fileId = storageClient1.upload_file1(filePath, "txt", null);
            //group1/M00/00/00/wKgZgV2u-nSAAmNiAAAACSOCv9M105.txt
            System.out.println(fileId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //下载文件
    @Test
    public void testDownload(){
        try {
            //初始化配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义TrackerClient用于请求TrackerServer
            TrackerClient trackerClient = new TrackerClient();
            //链接TrackerServer
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取storage
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            //创建storageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);

            String filePath = "F:/BugReport1.txt";
            String fileId = "group1/M00/00/00/wKgZgV2u-nSAAmNiAAAACSOCv9M105.txt";
            byte[] bytes = storageClient1.download_file1(fileId);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath));
            fileOutputStream.write(bytes);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
