package com.buzzinate.common.queue;

public class MessageWrapper<T> {
	private T message;
	private long deliveryTag;
	
	public MessageWrapper(T message, long deliveryTag) {
		this.message = message;
		this.deliveryTag = deliveryTag;
	}

	public T getMessage() {
		return message;
	}

	public long getDeliveryTag() {
		return deliveryTag;
	}
}
