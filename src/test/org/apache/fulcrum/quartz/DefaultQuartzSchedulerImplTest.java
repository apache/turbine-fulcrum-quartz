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
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
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
     * Make sure that the Quartz scheduler is up and running
     * 
     * @throws Exception generic exception
     */
	@Test
    public void testService() throws Exception
    {
        Scheduler scheduler = quartz.getScheduler();
        Assert.assertNotNull(scheduler);
        Assert.assertNotNull(scheduler.getContext());
    }
	

    /**
     * The following test has been updated for Quartz 2.3.0
     * Adding/removing a scheduled job
     * @throws Exception generic exception
     */
    @Test
    public void testAddRemoveTrigger() throws Exception
    {
        Scheduler scheduler = quartz.getScheduler();
        Assert.assertNotNull(scheduler);
        Assert.assertNotNull(scheduler.getContext());
        
    	// Define job instance
    	JobDetail job1 = JobBuilder.newJob(SimpleJob.class)
    	    .withIdentity("simpleJob", "TURBINE")
    	    .build();

    	// Define a Trigger that will fire "now", and not repeat
    	Trigger trigger = TriggerBuilder.newTrigger()
    	    .withIdentity("someTrigger", "TURBINE")
    	    .startNow()
    	    .build();

    	// Schedule the job with the trigger
    	scheduler.scheduleJob(job1, trigger);

    	TriggerKey triggerKey = new TriggerKey("someTrigger", "TURBINE");
        Trigger t1 = scheduler.getTrigger(triggerKey);
        
        // System.out.println(" >> KEY: " + t1.getJobKey().toString());
        
        Assert.assertNotNull(t1);
        scheduler.unscheduleJob(triggerKey);
        trigger = scheduler.getTrigger(triggerKey);
        Assert.assertNull(trigger);
    }
	
	
//
//    /**
//     * Get all scheduled jobs for "TURBINE" to make sure that
//     * the registration worked.
//     * @throws Exception generic exception 
//     */
//    @Test
//    public void testGetJobs() throws Exception
//    {
//        Scheduler scheduler = quartz.getScheduler();
//        Assert.assertNotNull(scheduler);
//        Set<JobKey> jobNames = scheduler.getJobKeys(GroupMatcher.jobGroupEquals("TURBINE"));
//        Assert.assertEquals("Expected two registered jobs", 2, jobNames.size());
//    }
//    
//
//    /**
//     * Get the job details and job data map of an existing job to
//     * make sure that the XStream configuration works.
//     * @throws Exception generic exception
//     */
//    @Test
//    public void testJobDetailMap() throws Exception
//    {
//    	
//        JobDetail jobDetail = quartz.getScheduler().getJobDetail(JobKey.jobKey("simpleJob", "TURBINE"));
//        Assert.assertNotNull(jobDetail);
//        Assert.assertEquals("simpleJob", jobDetail.getKey().getName());
//        Assert.assertNotNull(jobDetail.getJobDataMap());
//        Assert.assertEquals(2, jobDetail.getJobDataMap().size());
//    }
//
//    /**
//     * Make sure the "notSoSimpleJob" is triggered by the CronTrigger.
//     * @throws Exception generic exception
//     */
//    @Test
//    public void testGetTriggersOfJob() throws Exception
//    {
//        List<? extends Trigger> triggers = quartz.getScheduler().getTriggersOfJob(JobKey.jobKey("notSoSimpleJob", "TURBINE"));
//        Assert.assertEquals(1, triggers.size());
//        Assert.assertEquals("cronTrigger", ((Trigger)triggers.get(0)).getKey().getName());
//    }
//
//    /**
//     * Make sure that our two registered jobs are executed after
//     * one second.
//     * @throws Exception generic exception
//     */
//    @Test
//    public void testJobExecution() throws Exception
//    {
//        Thread.sleep(2000);
//        Assert.assertTrue("SimpleJob was not executed", SimpleJob.wasExecuted);
//        Assert.assertTrue("NotSoSimpleJob was not executed", NotSoSimpleJob.wasExecuted);
//        Assert.assertTrue("NotSoSimpleJob was not serviced", NotSoSimpleJob.wasServiced);
//        SimpleJob.reset();
//        NotSoSimpleJob.reset();
//        Thread.sleep(2000);
//        Assert.assertTrue("SimpleJob was not executed", SimpleJob.wasExecuted);
//        Assert.assertTrue("NotSoSimpleJob was not executed", NotSoSimpleJob.wasExecuted);
//        Assert.assertTrue("NotSoSimpleJob was not serviced", NotSoSimpleJob.wasServiced);
//    }
}
