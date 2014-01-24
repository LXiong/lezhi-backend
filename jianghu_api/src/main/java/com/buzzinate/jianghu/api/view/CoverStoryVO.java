package com.buzzinate.jianghu.api.view;

import java.util.List;

import com.buzzinate.jianghu.model.CoverStory;

public class CoverStoryVO {
    public long id;
    public int type;
    public String action;
    public String title;
    public String url;
    public String content;
    public String summary;
    public String backgroundUrl;
    public String lastUpdate;

    public CoverStoryVO(long id, int type, String title, String url, String content, String summary,
            String backgroundUrl, long lastUpdate, String action) {
        super();
        this.id = id;
        this.type = type;
        this.title = title;
        this.url = url;
        this.content = content;
        this.summary = summary;
        this.backgroundUrl = backgroundUrl;
        this.lastUpdate = String.valueOf(lastUpdate);
        this.action = action;
    }

    public static CoverStoryVO[] make(List<CoverStory> stories) {
        if (stories == null || stories.isEmpty()) {
            return new CoverStoryVO[0];
        }
        CoverStoryVO[] array = new CoverStoryVO[stories.size()];
        CoverStoryVO vo = null;
        CoverStory story = null;
        for (int i = 0; i < stories.size(); i++) {
            story = stories.get(i);
            vo = new CoverStoryVO(story.getId(), story.getType() == null ? -1 : story.getType().getCode(),
                    story.getTitle(), story.getUrl(), story.getContent(), story.getSummary(), story.getBackgroundUrl(),
                    story.getLastUpdate(), story.getAction().toString());
            array[i] = vo;
        }
        return array;
    }

    public static ExtCoverStoryVO make(List<CoverStory> stories, long lastUpadte) {
        return new ExtCoverStoryVO(make(stories), lastUpadte);
    }

    public static class ExtCoverStoryVO {

        public CoverStoryVO[] stories;
        public String lastUpdate;

        public ExtCoverStoryVO() {
        }

        public ExtCoverStoryVO(CoverStoryVO[] stories, long lastUpdate) {
            this.stories = stories;
            this.lastUpdate = String.valueOf(lastUpdate);
        }

    }

}
