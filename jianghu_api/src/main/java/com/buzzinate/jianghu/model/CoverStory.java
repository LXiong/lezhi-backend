package com.buzzinate.jianghu.model;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.buzzinate.jianghu.dao.mongo.StoryTypeConvert;
import com.google.code.morphia.annotations.Converters;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

/**
 * for cover api
 * 
 * @author chris.xue
 * 
 */
@JsonSerialize(using = CoverStorySerializer.class)
public class CoverStory {

    /**
     * 
     * @author chris.xue
     * 
     */
    @Converters(StoryTypeConvert.class)
    public enum StoryType {
        purePic(1), story(2), activity(3);
        @Id
        private final int code;

        private StoryType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static StoryType fromInt(int code) {
            switch (code) {
            case 1:
                return purePic;
            case 2:
                return story;
            case 3:
                return activity;
            default:
                return null;
            }
        }

        public static StoryType parseString(String codeName) {
            if (StringUtils.isEmpty(codeName)) {
                return null;
            }
            if (StringUtils.equals("purePic", codeName)) {
                return purePic;
            }
            if (StringUtils.equals("story", codeName)) {
                return story;
            }
            if (StringUtils.equals("activity", codeName)) {
                return activity;
            }
            return null;
        }

    }

    public enum StoryAction {
        WEBVIEW, ARCHIVE, APPSTORE
    }

    @Id
    private long id;
    private StoryType type;
    private StoryAction action;
    private String title;
    private String url;
    private String content;
    private String summary;
    private String backgroundUrl;
    @Indexed(unique = true, dropDups = true)
    private long lastUpdate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public StoryType getType() {
        return type;
    }

    public void setType(StoryType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    public long getLastUpdate() {
        if (lastUpdate > 0) {
            return lastUpdate;
        } else {
            return System.currentTimeMillis();
        }
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public StoryAction getAction() {
        return action;
    }

    public void setAction(StoryAction action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "CoverStory [id=" + id + ", type=" + type + ", action=" + action + ", title=" + title + ", url=" + url
                + ", content=" + content + ", summary=" + summary + ", backgroundUrl=" + backgroundUrl
                + ", lastUpdate=" + lastUpdate + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CoverStory other = (CoverStory) obj;
        if (id != other.id)
            return false;
        return true;
    }

}

class CoverStorySerializer extends JsonSerializer<CoverStory> {
    private ObjectMapper om = new ObjectMapper();

    @Override
    public void serialize(CoverStory object, JsonGenerator generator, SerializerProvider provider) throws IOException,
            JsonProcessingException {
        JsonNode node = om.valueToTree(object);
        om.writeTree(generator, node);
    }

}
