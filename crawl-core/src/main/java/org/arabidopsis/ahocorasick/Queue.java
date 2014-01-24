package org.arabidopsis.ahocorasick;

import java.util.ArrayList;


/**
   Quick-and-dirty queue class.  Essentially uses two lists to
   represent a queue.
 */
class Queue<E> {
    ArrayList<State<E>> l1;
    ArrayList<State<E>> l2;

    public Queue() {
	l1 = new ArrayList<State<E>>();
	l2 = new ArrayList<State<E>>();
    }

    public void add(State<E> s) {
	l2.add(s);
    }

    public boolean isEmpty() {
	return l1.isEmpty() && l2.isEmpty();
    }
    
    public State<E> pop() {
	if (isEmpty())
	    throw new IllegalStateException();
	if (l1.isEmpty()) {
	    for (int i = l2.size() - 1; i >= 0; i--)
		l1.add(l2.remove(i));
	    assert l2.isEmpty();
	    assert ! l1.isEmpty();
	}
	return l1.remove(l1.size() - 1);
    }
}