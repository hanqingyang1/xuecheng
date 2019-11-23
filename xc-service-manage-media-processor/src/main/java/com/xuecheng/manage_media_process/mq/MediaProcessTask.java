package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.bouncycastle.jcajce.provider.symmetric.RC5;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MediaProcessTask {

    @Value("${xc-service-manage-media.ffmpeg-path}")
    private String ffmpeg_path;
    @Value("${xc-service-manage-media.video-location}")
    private String serverPath;

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}",containerFactory = "customContainerFactory")
    public void receiveMediaProcessTask(String msg){
        //解析消息获取mediaId
        Map map = JSON.parseObject(msg, Map.class);
        String mediaId = (String) map.get("mediaId");
        //通过mediaId查询媒资信息
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if(!optional.isPresent()){
            return;
        }
        //获取媒资信息
        MediaFile mediaFile = optional.get();
        //获取文件类型
        String fileType = mediaFile.getFileType();
        if(fileType == null || !fileType.equals("avi")){
            mediaFile.setProcessStatus("303004");//无需处理
            mediaFileRepository.save(mediaFile);
        }else {
            mediaFile.setProcessStatus("303001");//处理中
            mediaFileRepository.save(mediaFile);
        }

        //使用工具类将avi文件转成MP4文件
        //String ffmpeg_path, String video_path, String mp4_name, String mp4folder_path
        //要处理的文件目录
        String video_path = serverPath + mediaFile.getFilePath()+mediaFile.getFileName();
        //生成的文件名称
        String mp4_name = mediaFile.getFileId()+".mp4";
        //生成的文件目录
        String mp4folder_path = serverPath + mediaFile.getFilePath();
        //创建文件处理工具类
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4folder_path);
        //执行处理
        String result = mp4VideoUtil.generateMp4();
        if(null == result || !"success".equals(result)){
            mediaFile.setProcessStatus("303004");
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }

        //生m3u8和ts文件
        //String ffmpeg_path, String video_path, String m3u8_name,String m3u8folder_path
        String mp4Video_path = serverPath + mediaFile.getFilePath()+mp4_name;
        String m3u8_name = mediaFile.getFileId()+".m3u8";
        String m3u8folder_path = serverPath+mediaFile.getFilePath()+"hls/";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path,mp4Video_path,m3u8_name,m3u8folder_path);
        //生成文件
        String lsResult = hlsVideoUtil.generateM3u8();
        if(null == lsResult || !"success".equals(lsResult)){
            mediaFile.setProcessStatus("303004");
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(lsResult);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }

        //获取ts文件列表
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        mediaFile.setProcessStatus("303002");
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        //获取m3u8文件执行路径.m3u8文件
        String m3u8File = mediaFile.getFilePath()+"hls/"+m3u8_name;
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        mediaFile.setFileUrl(m3u8File);

        mediaFileRepository.save(mediaFile);
    }
}
