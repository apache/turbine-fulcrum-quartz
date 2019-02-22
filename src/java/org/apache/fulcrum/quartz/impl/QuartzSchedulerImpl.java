package org.apache.fulcrum.quartz.impl;

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


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Matcher;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

/**
 * Avalon service  wrapping the QuartzScheduler.
 */
public class QuartzSchedulerImpl
        extends AbstractLogEnabled
        implements QuartzScheduler, Configurable, Serviceable, Disposable, Initializable, ThreadSafe, JobListener, Startable
{
    /** Configuration key */
    private static final String CONFIG_CONFIGURATION = "configuration";

    /** Configuration key */
    private static final String CONFIG_PROPERTY_FILE = "quartzPropertyFile";

    /** Configuration key */
    private static final String CONFIG_PROPERTIES = "properties";

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
    @Override
    public void configure(Configuration conf) throws ConfigurationException
    {
        Configuration quartzConf = conf.getChild(CONFIG_CONFIGURATION, true);

        if(quartzConf.getChild(CONFIG_PROPERTIES, false) != null)
        {
            this.quartzProperties = Parameters.toProperties(Parameters.fromConfiguration(quartzConf.getChild(CONFIG_PROPERTIES)));
        }
        else if(quartzConf.getChild(CONFIG_PROPERTY_FILE, false) != null)
        {
            this.quartzPropertyFile = quartzConf.getChild(CONFIG_PROPERTY_FILE).getValue();
        }
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        this.serviceManager = manager;
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    @Override
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
        getScheduler().getListenerManager().addJobListener(this, new ArrayList<Matcher<JobKey>>());
    }

    @Override
    public void start() throws Exception
    {
        getScheduler().start();

        if(getLogger().isInfoEnabled())
        {
            logSchedulerConfiguration();
        }

    }

    @Override
    public void stop() throws Exception
    {
        getScheduler().standby();
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    @Override
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
    @Override
    public Scheduler getScheduler()
    {
        return scheduler;
    }

    /**
     * Calls getName() on jobListener
     *
     * @see org.quartz.JobListener#getName()
     */
    @Override
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
    @Override
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
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException ex)
    {
        if (ex != null)
        {
            String msg = "Executing the job '" + context.getJobDetail().getKey() + "' failed";
            getLogger().error(msg, ex.getCause());
        }
        else
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Executing the job '" + context.getJobDetail().getKey() + "' took " + context.getJobRunTime() + " ms");
            }
        }
    }

    /**
     * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext context)
    {
        // nothing to do
    }

    // === Service Implementation ===========================================
    /**
     * @throws SchedulerException generic exception
     */
    private void logSchedulerConfiguration() throws SchedulerException
    {
        for (String jobGroup : getScheduler().getJobGroupNames())
        {
            Set<JobKey> jobsInGroup = getScheduler().getJobKeys(GroupMatcher.jobGroupEquals(jobGroup));
            getLogger().info("Job Group: " + jobGroup + " contains the following number of jobs : " + jobsInGroup.size());
            for (JobKey jobKey : jobsInGroup)
            {
                StringBuilder buffer = new StringBuilder();
                JobDetail jobDetail = getScheduler().getJobDetail(jobKey);
                List<? extends Trigger> jobTriggers = getScheduler().getTriggersOfJob(jobKey);
                buffer.append(jobDetail.getKey());
                buffer.append(" => ");
                if(jobTriggers != null && !jobTriggers.isEmpty())
                {
                    Trigger jt = jobTriggers.get(0);
                    buffer.append(jt.getKey());
                    buffer.append(" (");
                    buffer.append(jt.getNextFireTime());
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
