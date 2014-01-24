package com.buzzinate.main;

import static com.buzzinate.common.util.Constants.*;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.buzzinate.common.queue.Queue;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class QueueModule implements Module {
	private Logger log = Logger.getLogger(QueueModule.class);

	@Override
	public void configure(Binder binder) {
		try {
			bindQueue(binder, CRAWL_QUEUE);
			bindQueue(binder, CLASSIFY_QUEUE);
			bindQueue(binder, PREF_QUEUE);
		} catch (IOException e) {
			log.error(e);
		}
	}

	private void bindQueue(Binder binder, String queueName) throws IOException {
		Queue queue = new Queue("localhost", queueName);
		binder.bind(Queue.class).annotatedWith(Names.named(queueName)).toInstance(queue);
	}
}
