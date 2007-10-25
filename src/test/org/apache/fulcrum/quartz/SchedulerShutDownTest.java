/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fulcrum.quartz;

import java.util.Date;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

/**
 * @author <a href="mailto:leandro@ibnetwork.com.br">Leandro Rodrigo Saad Cruz</a>
 * @author <a href="mailto:epughNOSPAM@opensourceconnections.com">Eric Pugh </a>
 *
 */
public class SchedulerShutDownTest extends BaseQuartzTestCase implements SchedulerListener {

    public void testAddRemoveTrigger() throws Exception {
        QuartzScheduler quartz = (QuartzScheduler) lookup(QuartzScheduler.ROLE);
        Scheduler sched = quartz.getScheduler();
        Trigger someDay = new SimpleTrigger("someTrigger", "someGroup", "simpleJob", "DEFAULT_GROUP", new Date(3000, 1,
                1), null, 0, 0L);
        sched.scheduleJob(someDay);
        Trigger trigger = sched.getTrigger("someTrigger", "someGroup");
        assertNotNull(trigger);
        sched.unscheduleJob("someTrigger", "someGroup");
        trigger = sched.getTrigger("someTrigger", "someGroup");
        assertNull(trigger);
    }



    /*
     * (non-Javadoc)
     *
     * @see org.quartz.SchedulerListener#jobScheduled(org.quartz.Trigger)
     */
    public void jobScheduled(Trigger arg0) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.SchedulerListener#jobUnscheduled(java.lang.String,
     *      java.lang.String)
     */
    public void jobUnscheduled(String arg0, String arg1) {
        System.out.println("hi");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.SchedulerListener#triggerFinalized(org.quartz.Trigger)
     */
    public void triggerFinalized(Trigger arg0) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.SchedulerListener#triggersPaused(java.lang.String,
     *      java.lang.String)
     */
    public void triggersPaused(String arg0, String arg1) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.SchedulerListener#triggersResumed(java.lang.String,
     *      java.lang.String)
     */
    public void triggersResumed(String arg0, String arg1) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.SchedulerListener#jobsPaused(java.lang.String,
     *      java.lang.String)
     */
    public void jobsPaused(String arg0, String arg1) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.SchedulerListener#jobsResumed(java.lang.String,
     *      java.lang.String)
     */
    public void jobsResumed(String arg0, String arg1) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.SchedulerListener#schedulerError(java.lang.String,
     *      org.quartz.SchedulerException)
     */
    public void schedulerError(String arg0, SchedulerException arg1) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.SchedulerListener#schedulerShutdown()
     */
    public void schedulerShutdown() {
        System.out.println("ShutingDown scheduler !");
    }

}
