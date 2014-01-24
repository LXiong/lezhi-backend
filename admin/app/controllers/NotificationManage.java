package controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.buzzinate.common.util.JsonResults;

import model.Mention;
import model.SystemMessage;
import dao.SystemMessageDao;
import play.mvc.Controller;

public class NotificationManage extends Controller {
	public static Log log = LogFactory.getLog(NotificationManage.class);
	public static void index() {
		render();
	}
	
	public static void sendSystemMessage(String message) {
		try {
			JsonResults jr = new JsonResults();
			jr.set(false);
			SystemMessage msg = new SystemMessage();
			msg.message = message;
			SystemMessageDao.save(msg);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
}
