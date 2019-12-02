package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {


    @Autowired
    XcTaskRepository xcTaskRepository;
    @Autowired
    RabbitTemplate rabbitTemplate;


    /**
     * 查询指定时间前的n条记录
     * @param updateTime
     * @param size
     * @return
     */
    public List<XcTask> findXcTaskList(Date updateTime,int size){
        Pageable pageable = new PageRequest(0,size);

        Page<XcTask> all = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        List<XcTask> content = all.getContent();
        return content;
    }

    /**
     * 发布消息
     * @param xcTask
     * @param exchange
     * @param routingKey
     */
    @Transactional
    public void publish(XcTask xcTask,String exchange,String routingKey){

        //先查询任务是否存在
        Optional<XcTask> optional = xcTaskRepository.findById(xcTask.getId());
        if(optional.isPresent()){
            //发送消息
            rabbitTemplate.convertAndSend(exchange,routingKey,xcTask);
            //更新数据库updateTime
            XcTask task = optional.get();
            task.setUpdateTime(new Date());
            xcTaskRepository.save(task);
        }
    }

    /**
     * 获取任务
     * @param id
     * @param version
     * @return
     */
    @Transactional
    public int getTask(String id,int version){
        int count = xcTaskRepository.updateTaskVersion(id, version);
        return count;
    }
}
