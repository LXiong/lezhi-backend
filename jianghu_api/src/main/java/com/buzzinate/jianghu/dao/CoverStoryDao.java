package com.buzzinate.jianghu.dao;

import com.buzzinate.common.dao.BaseDao;
import com.buzzinate.jianghu.model.CoverStory;

public interface CoverStoryDao extends BaseDao<CoverStory, Long> {

    public long getLastUpdate();
    
    public long updateCoverStory(CoverStory story);
    
    public long saveCoverStory(CoverStory story);

}
