package com.buzzinate.crawl.queues;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.buzzinate.util.DomainNames;

/**
 * 管理多个队列，提供：
 *   提交抓取项
 *   获取抓取项抓取
 *   完成抓取
 *   
 * 典型的用法：
 * <pre>
 *   WorkQueues<CrawlTask> workQueues = new WorkQueues<CrawlTask>(1000)
 * 
 *   // thread 1
 *   for (CrawlTask task: tasks) {
 *   	workQueues.submit(url, msg);
 *   }
 *   
 *   // crawl thread 1
 *   while (!stopped.get()) {
 *   	CrawlTask<Message> task = null;
 *   	try {
 *   		task = queue.fetchTask();
 *   		if (task == null) {
 *   			Thread.sleep(1000s);
 *   			continue;
 *   		}
 *   		// crawl
 *   		queue.finishTask(task);
 *   	} catch (Exception e) {
 *   		queue.finishTask(task);
 *   	}
 *   }
 *   
 *   // crawl thread 2 .. n
 * </pre>
 * 
 * @author Brad Luo
 *
 */
public class WorkQueues<T> {
	private static final int QUEUE_SIZE = 1023;
	private static Logger log = Logger.getLogger(WorkQueues.class);
	
	private WorkQueue<T>[] queues = new WorkQueue[QUEUE_SIZE];
	private PriorityQueue<WorkQueue<T>> topQueue = new PriorityQueue<WorkQueue<T>>();
	
	private final int capacity;
	private int count = 0;
	private final Lock lock = new ReentrantLock();
	private final Condition notFull  = lock.newCondition();
			
	public WorkQueues(int capacity) {
		if (capacity <= 0) throw new IllegalArgumentException();
		this.capacity = capacity;
		for (int i = 0; i < queues.length; i++) queues[i] = new WorkQueue<T>(i);
	}
	
	private void submit(int qid, String url, T info) throws InterruptedException {
		CrawlTask<T> task = new CrawlTask<T>(qid, url, info);
		
		lock.lock();
		try {
			while (count >= capacity) {
				notFull.await();
			}
			
			WorkQueue<T> queue = queues[qid];
			if (!queue.isUsed() && queue.isEmpty()) {
				topQueue.offer(queue);
			}
			
			queue.offer(task);
			count++;
			log.debug(qid + " submit, now=" + count);
			if (queue.size() > 100) log.info("url >>> " + qid + ", size=" + queue.size());
		} finally {
			lock.unlock();
		}
	}

	public void submit(String url, T info) throws InterruptedException {
		int qid = makeQueue(url);
		if (queues[qid].size() > 200) qid = makeQueue2(url);
		submit(qid, url, info);
	}
	
	/**
	 * 获取待抓取任务，如果暂无，返回null
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public CrawlTask<T> fetchTask() throws InterruptedException {
		lock.lock();
		try {
			WorkQueue<T> queue = topQueue.poll();
			if (queue == null) {
//				log.debug("#### topQueue.size=" + topQueue.size());
				return null;
			}
			log.debug(queue.getQid() + " start, now=" + count);
			return queue.peek();
		} finally {
			lock.unlock();
		}
	}
	
	public void finishTask(CrawlTask<T> task) {
		int qid = task.getQid();
		lock.lock();
		try {
			WorkQueue<T> queue = queues[qid]; 
			
			queue.poll();
			if(!queue.isEmpty()) topQueue.offer(queue);
			
			count--;
			log.debug(qid + " done, now=" + count);
			notFull.signalAll();
		} finally {
			lock.unlock();
		}
	}
	
	public int getSize() {
		return count;
	}
	
	// TODO: Abstract to QueueAssignmentPolicy
	private int makeQueue(String url) {
		String host = DomainNames.safeGetHost(url);
		int h = host.hashCode() % QUEUE_SIZE;
		if (h < 0) h += QUEUE_SIZE;
		return h;
	}
	
	private int makeQueue2(String url) {
		int h = url.hashCode() % QUEUE_SIZE;
		if (h < 0) h += QUEUE_SIZE;
		return h;
	}
	
	public List<T> getAllItems() {
		ArrayList<T> items = new ArrayList<T>();
		for (WorkQueue<T> wq: queues) {
			for(CrawlTask<T> t: wq.getTasks()) {
				items.add(t.getInfo());
			}
		}
		return items;
	}
	
	public static void main(String[] args) throws InterruptedException {
		final WorkQueues<String> queues = new WorkQueues<String>(3);
		
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						CrawlTask<String> task = null;
						try {
							task = queues.fetchTask();
							if (task == null) {
								Thread.sleep(1000L);
								continue;
							}
							System.out.println("t1 crawl " + task.getInfo());
						} finally {
							if (task != null) queues.finishTask(task);
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		t1.start();
		
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						CrawlTask<String> task = null;
						try {
							task = queues.fetchTask();
							if (task == null) {
								Thread.sleep(1000L);
								continue;
							}
							System.out.println("t2 crawl " + task.getInfo());
						} finally {
							if (task != null) queues.finishTask(task);
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		t2.start();
		
		queues.submit("http://www.infoq.com/cn", "http://www.infoq.com/cn");
		queues.submit("http://www.infoq.com/", "http://www.infoq.com/");
		queues.submit("http://www.infoq.com/", "http://www.infoq.com/");
		queues.submit("http://www.baidu.com/", "http://www.baidu.com/");
		queues.submit("http://www.google.com.hk/", "http://www.google.com.hk/");
	}
}