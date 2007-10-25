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
import java.util.Map;

import org.apache.fulcrum.quartz.test.NotSoSimpleJob;
import org.apache.fulcrum.quartz.test.SimpleJob;
import org.quartz.CronTrigger;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

/**
 * @author <a href="mailto:leandro@ibnetwork.com.br">Leandro Rodrigo Saad Cruz</a>
 * @author <a href="mailto:epughNOSPAM@opensourceconnections.com">Eric Pugh </a>
 */
public class DefaultQuartzSchedulerImplTest extends BaseQuartzTestCase {


    public void testGetJobs() throws Exception {
        Scheduler sched = quartz.getScheduler();
        assertNotNull(sched);
        String[] jobNames = sched.getJobNames("DEFAULT_GROUP");
        assertEquals(2, jobNames.length);
    }

    public void testJobListener() throws Exception {
        Scheduler sched = quartz.getScheduler();
        //ExecutionManager tb conta
        assertEquals(2, sched.getGlobalJobListeners().size());
        JobListener listener = (JobListener) sched.getGlobalJobListeners().get(1);
        assertEquals("Foo", listener.getName());
    }

    public void testJobExecutionWithManager() throws Exception {
        Scheduler sched = quartz.getScheduler();
        Trigger rightNow = new SimpleTrigger("someTrigger", Scheduler.DEFAULT_GROUP, "notSoSimpleJob", "DEFAULT_GROUP",
                new Date(), null, 0, 0L);
        sched.scheduleJob(rightNow);
        Thread.sleep(500);
        assertTrue(NotSoSimpleJob.executed);
        assertTrue(NotSoSimpleJob.serviced);
    }

    public void testJobExecution() throws Exception {
        Scheduler sched = quartz.getScheduler();
        Trigger rightNow = new SimpleTrigger("someTrigger", Scheduler.DEFAULT_GROUP, "simpleJob", "DEFAULT_GROUP",
                new Date(), null, 0, 0L);
        sched.scheduleJob(rightNow);
        Thread.sleep(500);
        assertTrue(SimpleJob.executed);
        //can't compose
        assertFalse(SimpleJob.composed);
    }

    public void testJobExecutionWithCron() throws Exception {
        assertFalse(SimpleJob.executed);
        Scheduler sched = quartz.getScheduler();
        Trigger cronTrigger = new CronTrigger("someTriggerCron", Scheduler.DEFAULT_GROUP, "simpleJob", "DEFAULT_GROUP",
                "* * * * * ?");
        sched.scheduleJob(cronTrigger);
        Thread.sleep(500);
        assertTrue(SimpleJob.executed);
        //can't compose
        assertFalse(SimpleJob.composed);
    }

    public void testJobDetailMap() {
        Map map = quartz.getJobDetailsMap();
        assertEquals(2, map.size());
        assertTrue(map.containsKey("DEFAULT_GROUP.simpleJob"));
        assertTrue(map.containsKey("DEFAULT_GROUP.notSoSimpleJob"));
    }

    public void testTriggerMap() {
        Map map = quartz.getTriggersMap();
        assertEquals(2, map.size());
        assertTrue(map.containsKey("DEFAULT_GROUP.simpleTrigger"));

        SimpleTrigger simpleTrigger = (SimpleTrigger) map.get("DEFAULT_GROUP.simpleTrigger");
        assertEquals("DEFAULT_GROUP", simpleTrigger.getGroup());
        assertEquals("simpleTrigger", simpleTrigger.getName());

        assertTrue(map.containsKey("OTHER_GROUP.cron"));
        CronTrigger cronTrigger = (CronTrigger) map.get("OTHER_GROUP.cron");
        assertEquals("0 0 12 * * ?", cronTrigger.getCronExpression());
    }

    public void testPreScheduled() throws Exception {
        Trigger triggers[] = quartz.getScheduler().getTriggersOfJob("notSoSimpleJob", "DEFAULT_GROUP");
        assertEquals(1, triggers.length);

    }


}
