package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import jdk.nashorn.internal.runtime.regexp.joni.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CmsPageRepositoryTest {

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Test
    public void testFindAll(){
        List<CmsPage> all = cmsPageRepository.findAll();
        System.out.println(all);

    }
    @Test
    public void testFindPage(){

        Pageable pageable = PageRequest.of(0,10);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        System.out.println(all);

    }
    @Test
    public void testFindAllByExample(){

        Pageable pageable = PageRequest.of(0,10);

        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId("");
        cmsPage.setPageAliase("轮播");

        ExampleMatcher matching = ExampleMatcher.matching();
        matching.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<CmsPage> example = Example.of(cmsPage,matching);
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);
        System.out.println(all);

    }

}
