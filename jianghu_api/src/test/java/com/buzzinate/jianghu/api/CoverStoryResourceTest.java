package com.buzzinate.jianghu.api;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.buzzinate.common.util.IdGenerator;
import com.buzzinate.jianghu.config.JianghuModule;
import com.buzzinate.jianghu.dao.CoverStoryDao;
import com.buzzinate.jianghu.model.CoverStory;
import com.buzzinate.jianghu.model.CoverStory.StoryAction;
import com.buzzinate.jianghu.model.CoverStory.StoryType;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * 
 * @author chris.xue
 * 
 */
public class CoverStoryResourceTest {
    private CoverStoryDao dao;
    private Datastore ds;
    private  String[] imgs = new String[] {
            "http://img1.orangeworld.cn/upload/image/gg/b9/77/b9773589119d037e8932840bde41148d.jpg",
            "http://img1.orangeworld.cn/upload/image/gg/26/8b/268b556e7fcaab4dc931b13a7d33aeb5.jpg",
            "http://img1.orangeworld.cn/upload/image/gg/de/6d/de6dc46f8fae9fdbb8c2f56a7e1b5710.jpg" };

    public CoverStoryResourceTest() {
        Injector injector = Guice.createInjector(new JianghuModule());
        dao = injector.getInstance(CoverStoryDao.class);
        ds = injector.getInstance(Datastore.class);
    }

    @Test
    public void testSave() {
        CoverStory story = new CoverStory();
        story.setTitle("test");
        story.setType(StoryType.activity);
        story.setAction(StoryAction.ARCHIVE);
        story.setUrl("http://www.bshare.cn.com");
        story.setSummary("床前明月光，地上鞋两双");
        story.setTitle("封面故事");
        story.setBackgroundUrl(imgs[0]);
        story.setId(IdGenerator.generateLongId(ds, CoverStory.class));
        dao.saveCoverStory(story);

        story.setTitle("test");
        story.setType(StoryType.activity);
        story.setAction(StoryAction.WEBVIEW);
        story.setUrl("http://www.baidu.com");
        story.setSummary("床前明月光，地上鞋两双");
        story.setTitle("封面故事");
        story.setBackgroundUrl(imgs[1]);
        story.setId(IdGenerator.generateLongId(ds, CoverStory.class));
        dao.saveCoverStory(story);

        story.setTitle("test");
        story.setType(StoryType.activity);
        story.setAction(StoryAction.APPSTORE);
        story.setUrl("http://www.google.com");
        story.setSummary("床前明月光，地上鞋两双");
        story.setTitle("封面故事");
        story.setBackgroundUrl(imgs[2]);
        story.setId(IdGenerator.generateLongId(ds, CoverStory.class));
        dao.saveCoverStory(story);

    }

    // @Test
    public void testUpdate() {
        List<CoverStory> stories = dao.find().asList();
        Assert.assertNotNull(stories);
       
        for (int i = 0; i < imgs.length && i < stories.size(); i++) {
            CoverStory s = stories.get(i);
            s.setAction(StoryAction.ARCHIVE);
            // s.setUrl("http://www.baidu.com");
            // s.setAction(StoryAction.WEBVIEW);
            s.setBackgroundUrl(imgs[i]);
            dao.save(s);
        }


    }

}
