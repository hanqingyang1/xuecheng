package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 监听mq，接受页面发布的消息
 */
@Slf4j
@Component
public class ConsumerPostPage {

    @Autowired
    PageService pageService;

    @RabbitListener(queues = {"${xuecheng.mq.queue}"})
    public void postPage(String msg){
        Map map = JSON.parseObject(msg, Map.class);
        //得到消息id
        String pageId = (String) map.get("pageId");
        //检验数据
        CmsPage cmsPage = pageService.findCmsPageById(pageId);
        if(cmsPage == null){
            log.error("receive postPage msg cmsPage is null, pageId:{}",pageId);
            return;
        }
        //将页面从gridFs下载到服务器
        pageService.savePageToServerPath(pageId);
    }
}
