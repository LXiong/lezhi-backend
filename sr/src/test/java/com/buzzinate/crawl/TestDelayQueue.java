package com.buzzinate.crawl;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class TestDelayQueue {

	public static void main(String[] args) {
		DelayQueue<Item> tasks = new DelayQueue<Item>();
		long now = System.currentTimeMillis();
		tasks.put(new Item(now, 100));
		tasks.put(new Item(now, 10));
		tasks.put(new Item(now, 10));
		tasks.put(new Item(now, 1000));
		tasks.put(new Item(0, 1000));
		
		now = System.currentTimeMillis();
		long last = now - 50;
		while (!tasks.isEmpty()) {
			try {
				long d = last + 50 - System.currentTimeMillis();
				if (d > 0) Thread.sleep(d);
				
				Item i = tasks.take();
				System.out.println("item: " + i.getInterval());
				System.out.println("delay: " + (System.currentTimeMillis() - now));
				now = System.currentTimeMillis();
				last = now;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}

class Item implements Delayed {
	private long now;
	private int interval;
	
	public Item(long now, int interval) {
		this.now = now;
		this.interval = interval;
	}
	
	public int getInterval() {
		return this.interval;
	}

	@Override
	public int compareTo(Delayed o) {
		if (o == this) return 0;
		if (o instanceof Item) {
			Item i = (Item) o;
			long d = now + interval - i.now - i.interval;
			return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
		}
		else return -1;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(now + interval - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}
}
