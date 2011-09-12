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
import java.util.List;
import java.util.Set;

import org.apache.fulcrum.quartz.test.NotSoSimpleJob;
import org.apache.fulcrum.quartz.test.SimpleJob;
import org.quartz.DateBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

/**
 * @author <a href="mailto:leandro@ibnetwork.com.br">Leandro Rodrigo Saad Cruz</a>
 * @author <a href="mailto:epughNOSPAM@opensourceconnections.com">Eric Pugh </a>
 */
public class DefaultQuartzSchedulerImplTest extends BaseQuartzTestCase
{

    /**
     * Make sure that the Quartz sheduler is up and running
     */
    public void testService() throws Exception
    {
        Scheduler scheduler = quartz.getScheduler();
        assertNotNull(scheduler);
        assertNotNull(scheduler.getContext());
    }

    /**
     * Get all scheduled jobs for "TURBINE" to make sure that
     * the registration worked.
     */
    public void testGetJobs() throws Exception
    {
        Scheduler scheduler = quartz.getScheduler();
        assertNotNull(scheduler);
        Set jobNames = scheduler.getJobKeys(GroupMatcher.groupEquals("TURBINE"));
        assertEquals("Expected two registered jobs", 2, jobNames.size());
    }

    /**
     * Get the job details and job data map of an existing job to
     * make sure that the XStream configuration works.
     */
    public void testJobDetailMap() throws Exception
    {
        JobDetail jobDetail = quartz.getScheduler().getJobDetail(JobKey.jobKey("simpleJob", "TURBINE"));
        assertNotNull(jobDetail);
        assertEquals("simpleJob", jobDetail.getKey().getName());
        assertNotNull(jobDetail.getJobDataMap());
        assertEquals(2, jobDetail.getJobDataMap().size());
    }

    /**
     * Make sure the "notSoSimpleJob" is triggered by the CronTrigger.
     */
    public void testGetTriggersOfJob() throws Exception
    {
        List triggers = quartz.getScheduler().getTriggersOfJob(JobKey.jobKey("notSoSimpleJob", "TURBINE"));
        assertEquals(1, triggers.size());
        assertEquals("cronTrigger", ((Trigger)triggers.get(0)).getKey().getName());
    }

    /**
     * Test adding/removing a scheduled job which would be executed
     * in the future.
     */
    public void testAddRemoveTrigger() throws Exception
    {
        Scheduler scheduler = quartz.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey("someTrigger", "TURBINE");

        Date date = DateBuilder.dateOf(0, 0, 0, 1, 1, 2099);

        Trigger someDay = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .forJob("simpleJob", "TURBINE")
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInHours(1)
                .repeatForever())
            .startAt(date)
            .build();

        scheduler.scheduleJob(someDay);

        Trigger trigger = scheduler.getTrigger(triggerKey);
        assertNotNull(trigger);
        scheduler.unscheduleJob(triggerKey);
        trigger = scheduler.getTrigger(triggerKey);
        assertNull(trigger);
    }

    /**
     * Make sure that our two registered jobs are executed after
     * one second.
     */
    public void testJobExecution() throws Exception
    {
        Thread.sleep(2000);
        assertTrue("SimpleJob was not executed", SimpleJob.wasExecuted);
        assertTrue("NotSoSimpleJob was not executed", NotSoSimpleJob.wasExecuted);
        assertTrue("NotSoSimpleJob was not serviced", NotSoSimpleJob.wasServiced);
        SimpleJob.reset();
        NotSoSimpleJob.reset();
        Thread.sleep(2000);
        assertTrue("SimpleJob was not executed", SimpleJob.wasExecuted);
        assertTrue("NotSoSimpleJob was not executed", NotSoSimpleJob.wasExecuted);
        assertTrue("NotSoSimpleJob was not serviced", NotSoSimpleJob.wasServiced);
    }
}
