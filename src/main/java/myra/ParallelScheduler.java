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

/**
 * Parallel implementation of a <code>Scheduler</code>. Activites executed by
 * this scheduler need to be thread-safe.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class ParallelScheduler<T extends Comparable<T>>
	extends Scheduler<T> {
    /**
     * Tasks executor service.
     */
    private ExecutorService executor;

    /**
     * The array of candidate solutions.
     */
    private Object[] candidates;

    /**
     * Creates a new <code>ParallelScheduler</code>.
     */
    public ParallelScheduler() {
	super();
    }

    /**
     * Creates a new <code>ParallelScheduler</code> to run the specified
     * activity.
     * 
     * @param activity
     *            the activity to run.
     */
    public ParallelScheduler(Activity<T> activity) {
	super(activity);
    }

    @Override
    protected void initialise() {
	super.initialise();

	candidates = new Object[CONFIG.get(COLONY_SIZE)];

	// initialises the thread pool

	executor =
		Executors.newFixedThreadPool(CONFIG.get(PARALLEL),
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
    @SuppressWarnings("unchecked")
    protected void create() {
	final CountDownLatch latch =
		new CountDownLatch(CONFIG.get(COLONY_SIZE));

	for (int i = 0; i < CONFIG.get(COLONY_SIZE); i++) {
	    final int index = i;

	    executor.execute(new Runnable() {
		@Override
		public void run() {
		    T c = activity.create();
		    candidates[index] = c;

		    latch.countDown();
		}
	    });
	}

	try {
	    // waits for the creation of trails
	    latch.await();

	    for (Object o : candidates) {
		T current = (T) o;

		if (candidate == null || current.compareTo(candidate) > 0) {
		    candidate = current;
		}
	    }
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