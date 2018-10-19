/*
 * ParallelScheduler.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2015 Fernando Esteban Barril Otero
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package myra;

import static myra.Config.CONFIG;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import myra.Archive.SynchronizedArchive;

/**
 * Parallel implementation of a <code>Scheduler</code>. Activites executed by
 * this scheduler need to be thread-safe.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class ParallelScheduler<T extends Weighable<T>> extends Scheduler<T>
{
    /**
     * Tasks executor service.
     */
    private ExecutorService executor;

    /**
     * Creates a new <code>ParallelScheduler</code>.
     */
    public ParallelScheduler() {
	this(null, CONFIG.get(COLONY_SIZE));
    }

    /**
     * Creates a new <code>ParallelScheduler</code>.
     * 
     * @param capacity
     *            number of candidate solutions stored at each iteration.
     */
    public ParallelScheduler(int capacity) {
	this(null, capacity);
    }

    /**
     * Creates a new <code>ParallelScheduler</code>.
     * 
     * @param activity
     *            the (wrapped) activity.
     */
    public ParallelScheduler(Activity<T> activity) {
	this(activity, CONFIG.get(COLONY_SIZE));
    }

    /**
     * Creates a new <code>ParallelScheduler</code>.
     * 
     * @param activity
     *            the (wrapped) activity.
     * @param capacity
     *            number of candidate solutions stored at each iteration.
     */
    public ParallelScheduler(Activity<T> activity, int capacity) {
	super(activity, capacity);
    }

    @Override
    protected void initialise() {
	super.initialise();
	// initialises the thread pool
	executor = Executors.newFixedThreadPool(CONFIG.get(PARALLEL),
						new ThreadFactory() {
						    private int id = 0;

						    public Thread newThread(Runnable r) {
							return new Thread(r,
									  "ParallelScheduler::worker"
										  + (id++));
						    }
						});
    }

    @Override
    protected void create() {
	final CountDownLatch latch =
		new CountDownLatch(CONFIG.get(COLONY_SIZE));
	final Archive<T> pool = new SynchronizedArchive<>(archive);

	for (int i = 0; i < CONFIG.get(COLONY_SIZE); i++) {
	    executor.execute(new Runnable() {
		@Override
		public void run() {
		    pool.add(activity.create());
		    latch.countDown();
		}
	    });
	}

	try {
	    // waits for the creation of trails
	    latch.await();
	} catch (InterruptedException e) {
	    throw new RuntimeException(e);
	}
    }

    @Override
    public void run() {
	super.run();
	// finilizes the executor threads
	executor.shutdown();
    }
}