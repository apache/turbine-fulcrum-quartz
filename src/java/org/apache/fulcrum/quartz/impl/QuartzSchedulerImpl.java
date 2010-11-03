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

package org.apache.fulcrum.quartz.impl;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.fulcrum.quartz.QuartzScheduler;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Properties;

/**
 * Avalon service  wrapping the QuartzScheduler.
 */
public class QuartzSchedulerImpl
        extends AbstractLogEnabled
        implements QuartzScheduler, Configurable, Serviceable, Disposable, Initializable, ThreadSafe, JobListener, Startable
{
    /**
     * the Avalon service serviceManager
     */
    private ServiceManager serviceManager;

    /**
     * the Quartz scheduler instance
     */
    private Scheduler scheduler;

    /**
     * the quartz property file
     */
    private String quartzPropertyFile;

    /**
     * the quartz properties loaded from the XML configuration
     */
    private Properties quartzProperties;

    // === Avalon Lifecycle =================================================

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration conf) throws ConfigurationException
    {
        Configuration quartzConf = conf.getChild("configuration", true);

        if(quartzConf.getChild("properties", false) != null)
        {
            this.quartzProperties = Parameters.toProperties(Parameters.fromConfiguration(quartzConf.getChild("properties")));
        }
        else if(quartzConf.getChild("quartzPropertyFile", false) != null)
        {
            this.quartzPropertyFile = quartzConf.getChild("quartzPropertyFile").getValue();
        }
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException
    {
        this.serviceManager = manager;
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception
    {
        // instantiating a specific scheduler from a property file or properties
        StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
        if(this.quartzProperties != null)
        {
            getLogger().info("Pulling quartz configuration from the container XML configuration");
            schedulerFactory.initialize(this.quartzProperties);
        }
        else if(this.quartzPropertyFile != null)
        {
            getLogger().info("Pulling quartz configuration from the following property file : " + this.quartzPropertyFile);
            schedulerFactory.initialize(this.quartzPropertyFile);
        }
        else
        {
            getLogger().info("Using Quartz default configuration since no user-supplied configuration was found");            
            schedulerFactory.initialize();
        }
        
        this.scheduler = schedulerFactory.getScheduler();

        // add this service instance as JobListener to allow basic monitoring
        getScheduler().addGlobalJobListener(this);
    }

    public void start() throws Exception
    {
        getScheduler().start();

        if(getLogger().isInfoEnabled())
        {
            logSchedulerConfiguration();
        }

    }

    public void stop() throws Exception
    {
        getScheduler().standby();
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose()
    {
        try
        {
            // shutdown() does not return until executing Jobs complete execution
            this.scheduler.shutdown(true);
        }
        catch (SchedulerException e)
        {
            this.getLogger().warn("Problem shutting down quartz scheduler ", e);
        }

        this.scheduler = null;
        this.serviceManager = null;
    }

    // === Service Interface Implementation =================================

    /**
     * @see org.apache.fulcrum.quartz.QuartzScheduler#getScheduler()
     */
    public Scheduler getScheduler()
    {
        return scheduler;
    }

    /**
     * Calls getName() on jobListener
     *
     * @see org.quartz.JobListener#getName()
     */
    public String getName()
    {
        return getClass().getName();
    }

    /**
     * Hook to support jobs implementing Avalon interface such as
     * LogEnabled and Serviceable.
     *
     * @see org.quartz.JobListener#jobToBeExecuted(org.quartz.JobExecutionContext)
     */
    public void jobToBeExecuted(JobExecutionContext context)
    {
        Job job = context.getJobInstance();

        // inject a logger instance
        if(job instanceof LogEnabled)
        {
            ((LogEnabled) job).enableLogging(getLogger());
        }

        // inject a ServiceManager instance
        if (job instanceof Serviceable)
        {
            try
            {
                ((Serviceable) job).service(serviceManager);
            }
            catch (ServiceException e)
            {
                getLogger().error("Error servicing Job[" + job + "]", e);
            }
        }
    }

    /**
     * @see org.quartz.JobListener#jobWasExecuted(org.quartz.JobExecutionContext, org.quartz.JobExecutionException)
     */
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException ex)
    {
        if (ex != null)
        {
            String msg = "Executing the job '" + context.getJobDetail().getFullName() + "' failed";
            getLogger().error(msg, ex.getCause());
        }
        else
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Executing the job '" + context.getJobDetail().getFullName() + "' took " + context.getJobRunTime() + " ms");
            }
        }
    }

    /**
     * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
     */
    public void jobExecutionVetoed(JobExecutionContext context)
    {
        // nothing to do
    }

    // === Service Implementation ===========================================

    private void logSchedulerConfiguration() throws SchedulerException
    {
        String[] jobGroups = getScheduler().getJobGroupNames();
        for (int i = 0; i < jobGroups.length; i++)
        {
            String jobGroup = jobGroups[i];
            String[] jobsInGroup = getScheduler().getJobNames(jobGroup);
            getLogger().info("Job Group: " + jobGroup + " contains the following number of jobs : " + jobsInGroup.length);
            for (int j = 0; j < jobsInGroup.length; j++)
            {
                StringBuffer buffer = new StringBuffer();
                String jobName = jobsInGroup[j];
                JobDetail jobDetail = getScheduler().getJobDetail(jobName, jobGroup);
                Trigger[] jobTriggers = getScheduler().getTriggersOfJob(jobName, jobGroup);
                buffer.append(jobDetail.getFullName());
                buffer.append(" => ");
                if(jobTriggers != null && jobTriggers.length > 0)
                {
                    buffer.append(jobTriggers[0].getFullName());
                    buffer.append(" (");
                    buffer.append(jobTriggers[0].getNextFireTime());
                    buffer.append(")");
                }
                else
                {
                    buffer.append("no trigger defined");
                }

                getLogger().info(buffer.toString());
            }
        }        
    }
}
