package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * @author Administrator
 * @version 1.0
 **/
@Slf4j
@Service
public class MediaUploadService {
    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    String upload_location;
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routingkey_media_video;

    @Autowired
    RabbitTemplate rabbitTemplate;

    //得到文件所属目录路径
    public String getFileFolderPath(String fileMd5){
        return upload_location+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5;
    }

    public String getFilePath(String fileMd5,String fileExt){
        return upload_location+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/"+fileMd5+"."+fileExt;
    }

    //得到文件目录相对路径，路径中去掉根目录
    private String getFileFolderRelativePath(String fileMd5,String fileExt){
        String filePath = fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" +
                fileMd5 + "/";
        return filePath;
    }

    //得到块文件所属目录路径
    private String getChunkFileFolderPath(String fileMd5){
        return  upload_location + fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/chunk/";
    }
    /**
     * 文件上传前的注册，检查文件是否存在
     * 根据文件md5得到文件路径
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     * @param fileMd5 文件md5值
     * @param fileExt 文件扩展名
     * @return 文件路径
     */
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //得到文件所属目录
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        //得到文件路径
        String filePath = this.getFilePath(fileMd5, fileExt);
        //判断文件是否存在
        File file = new File(filePath);
        boolean exists = file.exists();

        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);

        if(exists && optional.isPresent()){
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        File fileFolder = new File(fileFolderPath);
        if(!fileFolder.exists()){
            fileFolder.mkdirs();
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //分块检查

    /**
     *
     * @param fileMd5 文件md5
     * @param chunk 块的下标
     * @param chunkSize 块的大小
     * @return
     */
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //获取快文件地址
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File chunkFile = new File(chunkFileFolderPath+chunk);
        if(chunkFile.exists()){
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK,true);
        }else {
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK,false);
        }
    }
    //上传分块
    public ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk) {
        //检查分块目录，如果不存在则要自动创建
        //得到分块目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //得到分块文件路径
        String chunkFilePath = chunkFileFolderPath + chunk;

        File chunkFileFolder = new File(chunkFileFolderPath);
        //如果不存在则要自动创建
        if(!chunkFileFolder.exists()){
            chunkFileFolder.mkdirs();
        }
        //得到上传文件的输入流
        InputStream inputStream = null;
        FileOutputStream outputStream  =null;
        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(new File(chunkFilePath));
            IOUtils.copy(inputStream,outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);

    }

    //合并文件
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //获取文件目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File fileFolder = new File(chunkFileFolderPath);
        //获取所有文件
        File[] files = fileFolder.listFiles();
        List<File> fileList = Arrays.asList(files);

        //获取文件路径
        String filePath = this.getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);

        //合并文件
        File file = this.mergeFile(fileList, mergeFile);
        if(file == null){
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }

        //校验MD5
        boolean md5 = this.checkFileMd5(file, fileMd5);
        if(!md5){
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //将文件信息保存到数据库
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5+"."+fileExt);
        mediaFile.setFileOriginalName(fileName);
        //文件路径保存相对路径
        mediaFile.setFilePath(getFileFolderRelativePath(fileMd5,fileExt));
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);

        //状态为上传成功
        mediaFile.setFileStatus("301002");
        MediaFile save = mediaFileRepository.save(mediaFile);
        sendProcessVideoMsg(mediaFile.getFileId());
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 发送视频处理消息
     * @param mediaId 文件id
     * @return
     */
    public ResponseResult sendProcessVideoMsg(String mediaId){

     //查询数据库mediaFile是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //封装消息体
        Map<String,Object> map = new HashMap<>();
        map.put("mediaId",mediaId);
        String jsonString = JSON.toJSONString(map);

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,jsonString);
            log.info("send media process task msg:{}",jsonString);

        }catch (Exception e){
            log.info("send media process task error,msg is:{},error:{}",jsonString,e.getMessage());
            return new ResponseResult(CommonCode.FAIL);
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //校验文件
    private boolean checkFileMd5(File mergeFile,String md5){
        try {
            //创建输入流
            FileInputStream inputStream = new FileInputStream(mergeFile);
            String md5Hex = DigestUtils.md5Hex(inputStream);
            if(md5.equalsIgnoreCase(md5Hex)){
                return true;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }
    //合并文件
    private File mergeFile(List<File> chunkFileList, File mergeFile) {
        try {
            //判断文件是否存在，存在删除，不存在创建
            if(mergeFile.exists()){
                mergeFile.delete();
            }else{
                mergeFile.createNewFile();
            }
            //文件排序
            Collections.sort(chunkFileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if(Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())){
                        return 1;
                    }
                    return -1;
                }
            });
            //文件合并
            RandomAccessFile ref_write = new RandomAccessFile(mergeFile,"rw");
            //创建缓冲区
            byte[] b = new byte[1024];
            for (File file : chunkFileList) {
                RandomAccessFile ref_read = new RandomAccessFile(file,"r");
                int len = -1;
                while((len = ref_read.read(b)) != -1){
                    ref_write.write(b,0,len);

                }
                ref_read.close();
            }
            ref_write.close();
            return mergeFile;
        }catch(Exception E) {
            E.printStackTrace();
            return null;
        }
    }
}
