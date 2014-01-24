package com.buzzinate.jianghu.api;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.annotations.GZIP;

import com.buzzinate.jianghu.api.view.CoverStoryVO;
import com.buzzinate.jianghu.api.view.CoverStoryVO.ExtCoverStoryVO;
import com.buzzinate.jianghu.dao.CoverStoryDao;
import com.buzzinate.jianghu.model.CoverStory;
import com.google.inject.Inject;

@Path("cover")
@Produces({ "application/json" })
public class CoverResource {
    private static Log log = LogFactory.getLog(CoverResource.class);
    @Inject
    private CoverStoryDao coverStoryDao;

    @GET
    @Path("list")
    @PermitAll
    @GZIP
    public ExtCoverStoryVO getCoverStory(@DefaultValue(value = "0") @QueryParam("lastUpdate") Long lastUpdate) {
        log.debug("lastUpdate=" + lastUpdate);
        if (lastUpdate == 0) {
            return findCoverStory();
        } else {
            long serverLastUpdate = getLastUpdate();
            if (serverLastUpdate != lastUpdate) {
                return findCoverStory();
            } else {
                throw new WebApplicationException(304);
            }
        }

    }

    private ExtCoverStoryVO findCoverStory() {
        List<CoverStory> stories = coverStoryDao.find(coverStoryDao.createQuery().order("-id")).asList();
        log.debug("get cover stories =[" + stories + "]");
        return CoverStoryVO.make(stories, getLastUpdate());
    }

    private long getLastUpdate() {
        return coverStoryDao.getLastUpdate();
    }

}
