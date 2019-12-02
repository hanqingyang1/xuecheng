package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Slf4j
@Component
public class ChooseCourseTask {

    @Autowired
    private TaskService taskService;

    @Scheduled(cron="0/3 * * * * *")//每隔3秒执行一次
    public void sendChoosecourseTask(){

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(GregorianCalendar.MINUTE,-1);
        Date time = calendar.getTime();
        List<XcTask> xcTaskList = taskService.findXcTaskList(time, 100);
        for (XcTask xcTask : xcTaskList) {
            if(taskService.getTask(xcTask.getId(),xcTask.getVersion())>0) {
                String exchange = xcTask.getMqExchange();
                String routingkey = xcTask.getMqRoutingkey();
                //发送消息
                taskService.publish(xcTask, exchange, routingkey);
            }
        }
    }

    @Scheduled(cron="0/3 * * * * *")//每隔3秒执行一次
    public void task1(){
        log.info("===============测试定时任务1开始===============");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("===============测试定时任务1结束===============");
    }
}
