package com.buzzinate.common.queue;

public interface Handler<T> {
	public void on(T m);
}
