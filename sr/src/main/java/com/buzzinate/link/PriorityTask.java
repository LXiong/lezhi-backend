package com.buzzinate.link;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public abstract class PriorityTask implements Delayed {
	public static final int NORMAL = 1;
	public static final int HIGH = 0;
	
	protected int priority;
	
	protected PriorityTask(int priority) {
		this.priority = priority;
	}

	@Override
	public int compareTo(Delayed o) {
		if (o == this) return 0;
		PriorityTask p = (PriorityTask) o;
		if (priority != p.priority) return priority - p.priority;
		long d = (getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
        return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
	}
}
