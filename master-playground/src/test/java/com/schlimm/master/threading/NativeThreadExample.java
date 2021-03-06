package com.schlimm.master.threading;

import com.schlimm.master.threading.model.Stock;
import com.schlimm.master.threading.model.StockAtomicLong;
import com.schlimm.master.threading.model.StockOwnedReadWriteLock;
import com.schlimm.master.threading.model.StockOwnedReentrantLock;
import com.schlimm.master.threading.model.StockSynchronized;
import com.schlimm.master.threading.model.StockUnsynchronized;

public class NativeThreadExample {

	private final static Stock[] stock = new Stock[] { new StockUnsynchronized(0), new StockOwnedReadWriteLock(0), new StockOwnedReentrantLock(0), new StockSynchronized(0), new StockAtomicLong(0) };

	public class StockIncreaser extends Thread {
		private int stockObjectIndex = 0;
		private volatile long added = 0;
		private boolean running = true;

		public StockIncreaser(String name, int stockObjectIndex) {
			super(name);
			this.stockObjectIndex = stockObjectIndex;
		}

		@Override
		public void run() {

			while (running) {
				if (Thread.currentThread().isInterrupted())
					running = false;
				try {
					stock[stockObjectIndex].add(1);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
				added += 1;
			}
		}
	}

	public class StockReducer extends Thread {
		private int stockObjectIndex = 0;
		private volatile long reduced = 0;
		private boolean running = true;

		public StockReducer(String name, int stockObjectIndex) {
			super(name);
			this.stockObjectIndex = stockObjectIndex;
		}

		@Override
		public void run() {
			while (running) {
				if (Thread.currentThread().isInterrupted())
					running = false;
				try {
					stock[stockObjectIndex].reduce(1);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
				reduced += 1;
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		System.out.println(String.format("%1$-30s %2$-10s %3$-12s %4$-12s %5$-14s %6$-12s", "Case", "Units", "Added", "Reduced", "Expected Units", "Difference"));
		for (int i = 0; i < stock.length; i++) {
			StockIncreaser thread1 = new NativeThreadExample().new StockIncreaser("QueuingStock-Increaser", i);
			thread1.start();
			StockReducer thread2 = new NativeThreadExample().new StockReducer("QueuingStock-Reducer", i);
			thread2.start();

			Thread.sleep(2000);

			thread1.interrupt();
			thread2.interrupt();

			Thread.sleep(100);

			System.out.println(String.format("%1$-30s %2$-10s %3$-12s %4$-12s %5$-14s %6$-12s", stock[i].getClass().getSimpleName(), stock[i].getUnits(), thread1.added, thread2.reduced, (thread1.added - thread2.reduced), stock[i].getUnits() -(thread1.added - thread2.reduced)));
		}
	}

}
