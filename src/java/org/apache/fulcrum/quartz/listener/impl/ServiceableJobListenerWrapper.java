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

package org.apache.fulcrum.quartz.listener.impl;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.fulcrum.quartz.listener.ServiceableJobListener;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;


/**
 * Wrapper for listeners to call compose on job instances that implement Composable
 *
 * @author <a href="mailto:leandro@ibnetwork.com.br">Leandro Rodrigo Saad Cruz</a>
 *
 */
public class  ServiceableJobListenerWrapper
	implements ServiceableJobListener
{
	protected JobListener wrappedListener;

    protected Logger logger;

    protected ServiceManager manager;

    /**
     *
     */
    public ServiceableJobListenerWrapper(JobListener listener)
    {
        wrappedListener = listener;
    }


    public void enableLogging(Logger logger)
    {
        this.logger = logger;
    }

    public void service(ServiceManager manager)
		throws ServiceException
	{
		this.manager = manager;
	}

	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.activity.Disposable#dispose()
	 */
	public void dispose()
	{
		manager = null;
		logger = null;
	}



    /**
     * Calls getName() on wrappedListener
     *
     * @see org.quartz.JobListener#getName()
     */
    public String getName()
    {
        return wrappedListener.getName();
    }

    /**
	 * Calls compose() on Job instance if it implements Composable
     * @see org.quartz.JobListener#jobToBeExecuted(org.quartz.JobExecutionContext)
     */
    public void jobToBeExecuted(JobExecutionContext context)
    {
    	Job job = context.getJobInstance();
		logger.debug("Job to be executed [" + job+"] Wrapped Listener ["+wrappedListener+"]");
		if(job instanceof Serviceable)
		{
			try
            {
                ((Serviceable)job).service(manager);
            }
            catch (ServiceException e)
            {
				logger.error("Error composing Job["+job+"]",e);
            }
		}
		wrappedListener.jobToBeExecuted(context);
    }

    /* (non-Javadoc)
     * @see org.quartz.JobListener#jobWasExecuted(org.quartz.JobExecutionContext, org.quartz.JobExecutionException)
     */
    public void jobWasExecuted(
        JobExecutionContext context,
        JobExecutionException ex)
    {
		Job job = context.getJobInstance();
		logger.debug("Job that was executed [" + job+"] Wrapped Listener ["+wrappedListener+"]");
		wrappedListener.jobWasExecuted(context,ex);
    }


    /* (non-Javadoc)
     * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
     */
    public void jobExecutionVetoed(JobExecutionContext context) {
        Job job = context.getJobInstance();
        logger.debug("Job that was executed [" + job+"] Wrapped Listener ["+wrappedListener+"]");
        wrappedListener.jobExecutionVetoed(context);
    }
}
