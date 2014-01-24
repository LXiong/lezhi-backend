package com.buzzinate.classify;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import com.buzzinate.common.dao.AreaArticleDao;
import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.mongo.ArticleDaoImpl;
import com.buzzinate.common.message.Message;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Category;
import com.buzzinate.common.model.Status;
import com.buzzinate.common.queue.Handler;
import com.buzzinate.common.queue.MessageWrapper;
import com.buzzinate.common.queue.Queue;
import com.buzzinate.common.util.Constants;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import static com.buzzinate.common.util.Constants.CLASSIFY_QUEUE;

public class ClassifyJob implements Runnable {
	private static Logger log = Logger.getLogger(ClassifyJob.class);
	
	private Queue classifyQueue;
	private ArticleDao articleDao;
	private AreaArticleDao areaArticleDao;
	
	@Inject(optional = true) @Named(Constants.KEY_MODEL_PATH)
	private String MODEL_PATH;
	
	private AtomicReference<Classifier> classifier = new AtomicReference<Classifier>();
	
	@Inject
	public ClassifyJob(Datastore ds, @Named(CLASSIFY_QUEUE) Queue classifyQueue) {
		this.classifyQueue = classifyQueue;
		this.articleDao = new ArticleDaoImpl(ds);
		this.areaArticleDao = new AreaArticleDao(ds);
		
	}

	@Override
	public void run() {
		try {
			log.info("Start to classify article ...");
			loadModel(MODEL_PATH + "/current");
			
			classifyQueue.receive(Message.class, new Handler<MessageWrapper<Message>>() {
				
				@Override
				public void on(MessageWrapper<Message> mw) {
					Message msg = mw.getMessage();
					try {
						Article article = articleDao.getPrimary(msg.pageId);
						Classifier c = classifier.get();
						if (c != null && article.getStatus() == Status.OK && article.getCategory() == Category.NONE) {
							Category cat = c.classify(article);
							log.info("result category: " + article.getId() + " ==> " + article.getTitle() + " : " + cat);
							articleDao.updateCategory(article.getId(), cat);
							areaArticleDao.updateCategory(article.getId(), cat);
						}
					} catch (Exception e) {
						log.error("Could not classify " + msg.pageId, e);
					}
				}
			}, true);
		} catch(IOException e) {
			log.error(e);
		}
	}
	
	private void loadModel(String modelPath) {
		log.info("model.path=" + modelPath);
		File mf = new File(modelPath);
		if (mf.exists()) {
			Classifier c = new Classifier();
			c.loadClassifier(modelPath);
			classifier.set(c);
			log.info("load model: " + modelPath);
		} else {
			log.info("Please train the classify model first");
		}
	}
}
