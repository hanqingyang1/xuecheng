package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import org.springframework.web.multipart.MultipartFile;

@Api("媒资上传接口")
public interface MediaUploadControllerApi {

    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt);

    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize);

    public ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk);

    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt);
}
