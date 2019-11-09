package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileSystemService {

    @Value("${xuecheng.fastdfs.tracker_servers}")
    String tracker_servers;
    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    int connect_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    int network_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.charset}")
    String charset;
    @Autowired
    FileSystemRepository fileSystemRepository;

    //上传文件
    public UploadFileResult upload(MultipartFile multipartFile,
                                   String filetag,
                                   String businesskey,
                                   String metadata){
        if(multipartFile == null){
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }
        //1.将文件上传到fdfs中
        String fileId = fdfs_upload(multipartFile);
        if(StringUtils.isBlank(fileId)){
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
        }
        //将文件信息保存到mongoDb
        FileSystem fileSystem = new FileSystem();
        fileSystem.setFileId(fileId);
        fileSystem.setFilePath(fileId);
        fileSystem.setBusinesskey(businesskey);
        fileSystem.setFiletag(filetag);
        fileSystem.setFileType(multipartFile.getContentType());
        fileSystem.setFileName(multipartFile.getOriginalFilename());
        if(StringUtils.isNotBlank(metadata)){
            try {
                Map map = JSON.parseObject(metadata, Map.class);
                fileSystem.setMetadata(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        fileSystemRepository.save(fileSystem);
        return new  UploadFileResult(CommonCode.SUCCESS,fileSystem);
    }

    /**
     * 上传文件到fdfs
     * @param multipartFile  文件
     * @return fileId
     */
    public String fdfs_upload(MultipartFile multipartFile){
        //初始化文件系统
        initFdfsConfig();
        //创建TrackClient
        TrackerClient trackerClient = new TrackerClient();
        try {
            //创建链接
            TrackerServer trackerServer = trackerClient.getConnection();
            //获得storeStorage服务器
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建storeClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storeStorage);

            //获取文件字节数据
            byte[] bytes = multipartFile.getBytes();
            //获取文件扩展名
            String filename = multipartFile.getOriginalFilename();
            String ext = filename.substring(filename.lastIndexOf(".") + 1);

            String fileId = storageClient1.upload_file1(bytes, ext, null);
            return fileId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 初始化fdfs
     */
    public void initFdfsConfig(){
        try {
            ClientGlobal.initByTrackers(tracker_servers);
            ClientGlobal.setG_charset(charset);
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
        }catch (Exception e){
            e.printStackTrace();
            //初始化文件系统出错
            ExceptionCast.cast(FileSystemCode.FS_INITFDFSERROR);
        }
    }
}
