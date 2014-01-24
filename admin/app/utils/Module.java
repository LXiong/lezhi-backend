package utils;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.buzzinate.common.queue.Queue;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.mongodb.MongoException;

public class Module extends AbstractModule {
	private Logger log = Logger.getLogger(Module.class);
	@Override
	protected void configure() {
		try {
			bindQueue(Constants.LINK_QUEUE);
			bindQueue(Constants.PAGE_QUEUE);
			bindQueue(Constants.CLASSIFY_QUEUE);
			bindQueue(Constants.SR_QUEUE);  
			bindQueue(Constants.USER_QUEUE);
			bindQueue(Constants.MODEL_QUEUE);
		} catch (MongoException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
	}

	
	private void bindQueue(String queueName) throws IOException {
		Queue queue = new Queue("localhost", queueName);
		bind(Queue.class).annotatedWith(Names.named(queueName)).toInstance(queue);
	}

}
