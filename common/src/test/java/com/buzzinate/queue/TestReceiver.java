package com.buzzinate.queue;

import org.apache.log4j.Logger;

import com.buzzinate.common.queue.Handler;
import com.buzzinate.common.queue.MessageWrapper;
import com.buzzinate.common.queue.Queue;

public class TestReceiver {
	private static Logger log = Logger.getLogger(TestReceiver.class);

	public static void main(String[] args) throws Exception {
		final Queue queue = new Queue("localhost", "test");
		
		queue.startReceive(Task.class, new Handler<MessageWrapper<Task>>() {
			@Override
			public void on(MessageWrapper<Task> msg) {
				log.info("deliveryTag:" + msg.getDeliveryTag());
				log.info("Receive task:" + msg.getMessage().id + ", name=" + msg.getMessage().name);
				log.info("Owner:" + msg.getMessage().owner.user);
			}
		}, false);
		
		Task task = new Task();
		task.id = 100;
		task.name = "implement sr";
		task.owner = new Owner();
		task.owner.user = "brad";
		queue.send(task);
		
		//Thread.sleep(10000);
		//queue.close();
	}
}
