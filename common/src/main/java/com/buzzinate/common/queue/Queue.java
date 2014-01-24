package com.buzzinate.common.queue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class Queue {
	private static Logger log = Logger.getLogger(Queue.class);
	
	private Connection connection;
	private Channel channel;
	
	private String queue;
	private String routingKey;
	
	private ObjectMapper mapper = new ObjectMapper();

	public Queue(String host, String queue) throws IOException {
		this.queue = queue;
		this.routingKey = queue;
		
		ConnectionFactory factory = new ConnectionFactory();
//		factory.setRequestedHeartbeat(5);
//		factory.setConnectionTimeout(1000 * 30);
		factory.setHost(host);
		connection = factory.newConnection();
		channel = connection.createChannel();
		
		channel.exchangeDeclare("exchange", "direct", true);
		channel.queueDeclare(queue, true, false, false, null);
		channel.queueBind(queue, "exchange", routingKey);
		log.info("Message queue connected!");
	}

	public <T> void send(T msg) throws IOException {
		String toSent = mapper.writeValueAsString(msg);
		synchronized (channel) {
			channel.basicPublish("exchange", routingKey, null, toSent.getBytes());
		}
	}
	
	public void commit(long deliveryTag) throws IOException {
		channel.basicAck(deliveryTag, false);
	}

	/**
	 * !NOTE important, this is block operation，会阻塞当前线程
	 * 
	 * @param clazz
	 * @param msgReceiver
	 * @param stopped
	 * @throws IOException
	 */
	public <T> void receive(final Class<T> clazz, final Handler<MessageWrapper<T>> msgReceiver, boolean autoAck) throws IOException {
		final QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queue, false, consumer);
		
		log.info(queue + " message handler started!");
		while (!Thread.interrupted()) {
		    try {
		    	QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		    	if(autoAck) {
		    		synchronized(channel) {
		    			//TODO: 需要最后应答，以避免异常时消息丢失？但是，重发同消息怎么处理？
		    			channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		    		}
		    	}
				        
				byte[] data = delivery.getBody();
				T msg = mapper.readValue(new String(data), clazz);
				msgReceiver.on(new MessageWrapper<T>(msg, delivery.getEnvelope().getDeliveryTag()));
			} catch (InterruptedException e) {
			 	log.error("InterruptedException: ", e);
			    return;
			} catch (Exception e) {
				log.error("Receive error", e);
			}
		}
	}
	
	public <T> void startReceive(final Class<T> clazz, final Handler<MessageWrapper<T>> msgReceiver, final boolean autoAck) throws IOException {
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					receive(clazz, msgReceiver, autoAck);
				} catch (IOException e) {
					log.error("error", e);
				}
			}			
		}).start();
	}
	
	public <T> void receive(final long delay, final Class<T> clazz, final Handler<List<MessageWrapper<T>>> msgReceiver, boolean autoAck) throws IOException {
		final QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queue, false, consumer);
		
		log.info(queue + " message handler started!");
		long lastFetchTime = 0;
		while (!Thread.interrupted()) {
			 try {
		    	long sleep = lastFetchTime + delay - System.currentTimeMillis();
		    	if (sleep > 0) Thread.sleep(sleep);
		    	
		    	List<MessageWrapper<T>> msgs = new ArrayList<MessageWrapper<T>>();
		    	for (int i = 0; i < 1000; i++) {
		    		QueueingConsumer.Delivery delivery = consumer.nextDelivery(10);
		    		if (delivery == null) break;
		    		byte[] data = delivery.getBody();
			    	T msg = mapper.readValue(new String(data), clazz);
			    	msgs.add(new MessageWrapper<T>(msg, delivery.getEnvelope().getDeliveryTag()));
			    	if (autoAck) {
			    		synchronized(channel) {
			    			// TODO: 需要最后应答，以避免异常时消息丢失？但是，重发同消息怎么处理？
			    			channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			    		}
			    	}
		    	}
		    	lastFetchTime = System.currentTimeMillis();
		    	log.info(queue + " fetch messages:" + msgs.size());
		        
		        msgReceiver.on(msgs);
		    } catch (InterruptedException e) {
		    	log.error("InterruptedException: ", e);
		    } catch (Exception e) {
		    	log.error("Delay Receive error: ", e);
		    }
		}
	}
	
	public <T> void startReceive(final long delay, final Class<T> clazz, final Handler<List<MessageWrapper<T>>> msgReceiver, final boolean autoAck) throws IOException {
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					receive(delay, clazz, msgReceiver, autoAck);
				} catch (IOException e) {
					log.error("error", e);
				}
			}			
		}).start();
	}
	
	public void close() {
		try {
			channel.close();
			connection.close();
		} catch (IOException e) {
			// ignore
		}
	}
}
