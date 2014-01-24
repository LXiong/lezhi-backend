package controllers;

import static utils.Constants.ROLE_ADMIN;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.buzzinate.common.model.BlackSite;

import dao.BlackSiteDao;
import play.mvc.Controller;
import play.mvc.With;

@Check(ROLE_ADMIN)
@With(Secure.class)
public class BlackSiteManager extends Controller {
	private static Log log = LogFactory.getLog(BlackSiteManager.class);
	
	public static void index(int offset, int length) {
		if (length == 0) length = 20;
		List<BlackSite> blackSites = BlackSiteDao.getTopUnverified(offset, length);
		int nextOffset = offset + length;
		render(blackSites, nextOffset);
	}
	
	public static void verifyBlackSites(List<String> blackSites) {
		log.info("Verify following blackSites:" + blackSites);
		BlackSiteDao.verify(blackSites);
		index(0, 0);
	}
}