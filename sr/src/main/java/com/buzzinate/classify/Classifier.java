package com.buzzinate.classify;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.mahout.classifier.sgd.AdaptiveLogisticRegression;
import org.apache.mahout.classifier.sgd.CrossFoldLearner;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.ModelSerializer;
import org.apache.mahout.ep.State;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;
import org.apache.mahout.vectorizer.encoders.WordValueEncoder;
import org.jsoup.Jsoup;

import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Category;
import com.buzzinate.nlp.chinese.Token;
import com.buzzinate.nlp.chinese.WordSegmenter;

/**
 * Train the classification model
 * 
 * @author zyeming
 *
 */
public class Classifier {
	
	private static final int LEARNING_WINDOW = 500;
	private static final int LEARNING_INTERVAL = 1000;
	private static final int NUM_CATEGORIES = 9;
	private static final int NUM_FEATURES = 50000;
	private static final int NUM_REPEAT = 2;
	private static final double DEFAULT_CRIATERIA = 0.0;
	
	private static Logger log = Logger.getLogger(Classifier.class);
	
	private static WordValueEncoder encoder = new StaticWordValueEncoder("body");
	
	private CrossFoldLearner classifier;
	
	public Classifier() { }
	
	synchronized public void setClassifier(CrossFoldLearner classifier) {
		this.classifier = classifier;
	}
	
	public Category classify(Article article) {
		return classify(article, DEFAULT_CRIATERIA);
	}
	
	public boolean loadClassifier(String path) {
		try {
			InputStream in = new FileInputStream(path);
			CrossFoldLearner classifier = ModelSerializer.readBinary(in, CrossFoldLearner.class);
			setClassifier(classifier);
			return true;
		} catch (Exception e) {
			setClassifier(null);
			log.error("Failed to load the model.", e);
			return false;
		} 
	}
	
	public boolean saveClassifier(String path) {
		if (classifier == null) {
			return false;
		}
		
		try {
			ModelSerializer.writeBinary(path, classifier);
			return true;
		} catch (IOException e) {
			log.error("Failed to save the model.", e);
			return false;
		}
	}
	
	synchronized public Category classify(Article article, double criteria) {
		if (classifier == null) {
			throw new IllegalStateException("Classifier is not trained or loaded!");
		}
        try {
			String trainingContent = getTrainingContent(article);
			if (StringUtils.isEmpty(trainingContent) || isEnglish(trainingContent)) {
				return Category.NONE;
			}
        	Vector vec = encodeTextToVector(trainingContent);
        	Vector p = classifier.classifyFull(vec);
        	log.debug(p.toString());
        	int target = p.maxValueIndex();
        	if (p.get(target) < criteria) {
        		return Category.NONE;
        	} else {
        		return Category.getCategory(target);
        	}
        } catch (IOException e) {
        	log.error("Exception while doing classify.", e);
        	return Category.NONE;
        }
	}
	
	public boolean train(List<Article> articles) {
		AdaptiveLogisticRegression learningAlgorithm =
			new AdaptiveLogisticRegression(NUM_CATEGORIES, NUM_FEATURES, new L1());
		learningAlgorithm.setInterval(LEARNING_INTERVAL);
		learningAlgorithm.setAveragingWindow(LEARNING_WINDOW);
		
		List<Article> trainingSet = new ArrayList<Article>();
		for (int i = 0; i < NUM_REPEAT; i++) {
			trainingSet.addAll(articles);
		}
		Collections.shuffle(trainingSet);
		
		log.info("Starting learning...");
		int k = 0;
		for (Article a : trainingSet) {
			try {
				String trainingContent = getTrainingContent(a);
				if (StringUtils.isEmpty(trainingContent) || isEnglish(trainingContent)) {
					continue;
				}
	        	Vector vec = encodeTextToVector(trainingContent);
				learningAlgorithm.train(a.getCategory().getCode(), vec);
	            
	           	State<AdaptiveLogisticRegression.Wrapper, CrossFoldLearner> best =
	        		learningAlgorithm.getBest();
	            if (best != null && (k % LEARNING_WINDOW == 0)) {
	            	CrossFoldLearner model = best.getPayload().getLearner();
	            	double averageCorrect = model.percentCorrect();
	            	double averageLL = model.logLikelihood();
	            	log.debug(String.format("Learning now: %d\t%.3f\t%.2f\n", k, averageLL, averageCorrect * 100));
	            }
	            k++;
			} catch (IOException e) {
				log.warn("Exception while learning.", e);
			}
        }
		log.info("Finished learning...");
		
		State<AdaptiveLogisticRegression.Wrapper, CrossFoldLearner> best =
    		learningAlgorithm.getBest();
		if (best != null) {
			CrossFoldLearner learner = best.getPayload().getLearner();
			setClassifier(learner);
			return true;
		} else {
			log.error("Training failed, might because of too few training data.");
			return false;
		}
	}
	
	private static boolean isEnglish(String text) {
		int nLetter = 0;
		int nAscii = 0;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			int type = Character.getType(ch);
			if (type == Character.UPPERCASE_LETTER || type == Character.LOWERCASE_LETTER) nAscii++;
			if (Character.isLetter(ch)) nLetter++;
		}
		
		return nAscii >= nLetter * 0.8;
	}
	
	private static String getTrainingContent(Article article) {
		StringBuilder c = new StringBuilder("");
		// title + keywords + summary seems the best combination
		if (!StringUtils.isEmpty(article.getTitle())) {
			c.append(article.getTitle()).append(" ");
		}
		if (!StringUtils.isEmpty(article.getKeywords())) {
			c.append(article.getKeywords()).append(" ");
		}
		if (!StringUtils.isEmpty(article.getContent())) {
			String text = Jsoup.parse(article.getContent()).body().text();
			c.append(text).append(" ");
		}
		return c.toString();
	}
	
	private static Vector encodeTextToVector(String text) throws IOException {
		Vector v = new RandomAccessSparseVector(NUM_FEATURES);
		List<Token> tokens = WordSegmenter.segmentSentence(text);
		for (Token token: tokens) {
			encoder.addToVector(token.getWord(), 1, v);
		}
		return v;
	}
	
}
