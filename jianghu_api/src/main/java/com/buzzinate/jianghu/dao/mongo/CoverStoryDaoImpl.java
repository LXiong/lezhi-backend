package com.buzzinate.jianghu.dao.mongo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.jianghu.dao.CoverStoryDao;
import com.buzzinate.jianghu.model.CoverStory;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.inject.Inject;

/**
 * 
 * @author chris.xue
 * 
 */
public class CoverStoryDaoImpl extends BaseDaoDefault<CoverStory, Long> implements CoverStoryDao {
    public static final String TAG = "CoverStoryDao::exception";
    private static Log log = LogFactory.getLog(CoverStoryDaoImpl.class);

    @Inject
    public CoverStoryDaoImpl(Datastore ds) {
        super(ds);
    }

    @Override
    public long getLastUpdate() {
        CoverStory story = findOne(createQuery().order("-lastUpdate"));
        if (story != null) {
            return story.getLastUpdate();
        }
        return 0;
    }

    @Override
    public long updateCoverStory(CoverStory story) {
        if (story == null) {
            return 0;
        }
        log.debug("update story==>[" + story.toString() + "]");
        long current = System.currentTimeMillis();
        story.setLastUpdate(current);

        Query<CoverStory> query = createQuery();
        query.filter("_id", story.getId());
        UpdateOperations<CoverStory> operations = createUpdateOperations();
        boolean needUpdate = false;
        if (story.getBackgroundUrl() != null) {
            needUpdate = true;
            operations.set("backgroundUrl", story.getBackgroundUrl());
        }
        if (story.getContent() != null) {
            needUpdate = true;
            operations.set("content", story.getContent());
        }
        if (story.getSummary() != null) {
            needUpdate = true;
            operations.set("summary", story.getSummary());
        }
        if (story.getTitle() != null) {
            needUpdate = true;
            operations.set("title", story.getTitle());
        }
        if (story.getType() != null) {
            needUpdate = true;
            operations.set("type", story.getType().getCode());
        }
        if (story.getAction() != null) {
            needUpdate = true;
            operations.set("action", story.getAction().toString());
        }
        if (story.getUrl() != null) {
            needUpdate = true;
            operations.set("url", story.getUrl());
        }
        if (needUpdate) {
            operations.set("lastUpdate", current);
            update(query, operations);
        }
        return current;

    }

    @Override
    public long saveCoverStory(CoverStory story) {
        if (story == null) {
            return 0;
        }
        log.debug("save story==>[" + story.toString() + "]");
        long current = System.currentTimeMillis();
        story.setLastUpdate(current);
        save(story);
        return current;

    }

}
