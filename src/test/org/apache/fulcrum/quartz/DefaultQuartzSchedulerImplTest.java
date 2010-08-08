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

import org.apache.fulcrum.quartz.test.NotSoSimpleJob;
import org.apache.fulcrum.quartz.test.SimpleJob;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;

import java.util.Date;

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
        String[] jobNames = scheduler.getJobNames("TURBINE");
        assertEquals("Expected two registered jobs", 2, jobNames.length);
    }

    /**
     * Get the job details and job data map of an existing job to
     * make sure that the XStream configuration works.
     */
    public void testJobDetailMap() throws Exception
    {
        JobDetail jobDetail = quartz.getScheduler().getJobDetail("simpleJob", "TURBINE");
        assertNotNull(jobDetail);
        assertEquals("simpleJob", jobDetail.getName());
        assertNotNull(jobDetail.getJobDataMap());
        assertEquals(2, jobDetail.getJobDataMap().size());
    }

    /**
     * Make sure the "notSoSimpleJob" is triggered by the CronTrigger.
     */
    public void testGetTriggersOfJob() throws Exception
    {
        Trigger triggers[] = quartz.getScheduler().getTriggersOfJob("notSoSimpleJob", "TURBINE");
        assertEquals(1, triggers.length);
        assertEquals("cronTrigger", triggers[0].getName());
    }

    /**
     * Test adding/removing a scheduled job which would be executed
     * in the future.
     */
    public void testAddRemoveTrigger() throws Exception
    {
        Scheduler scheduler = quartz.getScheduler();
        Date date = TriggerUtils.getDateOf(0, 0, 0, 1, 1, 2099);
        Trigger someDay = new SimpleTrigger("someTrigger", "TURBINE", "simpleJob", "TURBINE", date, null, 0, 0L);
        scheduler.scheduleJob(someDay);
        Trigger trigger = scheduler.getTrigger("someTrigger", "TURBINE");
        assertNotNull(trigger);
        scheduler.unscheduleJob("someTrigger", "TURBINE");
        trigger = scheduler.getTrigger("someTrigger", "TURBINE");
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
