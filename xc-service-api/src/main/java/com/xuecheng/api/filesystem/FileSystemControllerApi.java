package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.multipart.MultipartFile;

@Api(value="文件管理接口",description = "文件管理接口，提供文件管理、查询接口")
public interface FileSystemControllerApi {


    /**
    * 上传文件
    * @param multipartFile 文件
    * @param filetag 文件标签
    * @param businesskey 业务key
    * @param metadata 元信息,json格式
    * @return
    */
    @ApiOperation("上传文件")
    public UploadFileResult upload(MultipartFile multipartFile,
                                   @ApiParam("文件标签") String filetag,
                                   @ApiParam("业务key")String businesskey,
                                   @ApiParam("元信息,json格式")String metadata);


}