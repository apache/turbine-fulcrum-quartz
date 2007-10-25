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

//JDK
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationSerializer;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.fulcrum.quartz.QuartzScheduler;
import org.apache.fulcrum.quartz.listener.ServiceableJobListener;
import org.apache.fulcrum.quartz.listener.impl.ServiceableJobListenerWrapper;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Default implementation of QuartzScheduler
 *
 * @author <a href="mailto:leandro@ibnetwork.com.br">Leandro Rodrigo Saad Cruz
 *         </a>
 * @author <a href="mailto:epughNOSPAM@opensourceconnections.com">Eric Pugh </a>
 *
 */
public class DefaultQuartzScheduler
    extends AbstractLogEnabled
    implements QuartzScheduler, Configurable, Serviceable, Disposable, Initializable, ThreadSafe {

    private ServiceableJobListener wrapper;

    private ServiceManager manager;

    private String globalJobListenerClassName;

    private Scheduler scheduler;

    private Configuration jobDetailsConf;

    private Configuration triggersConf;

    private Map jobDetailsMap;

    private Map triggersMap;

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration conf) throws ConfigurationException {

        jobDetailsConf = conf.getChild("jobDetails").getChild("list");
        triggersConf = conf.getChild("triggers").getChild("list");
        Configuration child = conf.getChild("globalJobListener", false);
        if (child != null) {
            globalJobListenerClassName = conf.getChild("globalJobListener").getAttribute("className");
        }
    }

    /**
     * @see org.apache.avalon.framework.component.Composable#compose(org.apache.avalon.framework.component.ComponentManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        //factory = (FactoryService) manager.lookup(FactoryService.ROLE);

        SchedulerFactory schedFact = new StdSchedulerFactory();
        try {
            scheduler = schedFact.getScheduler();
        } catch (SchedulerException e) {
            throw new ServiceException("QuartzScheduler", "Error composing scheduler instance", e);
        }

    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        XStream xstream = new XStream(new DomDriver()); // does not require XPP3
        // library

        DefaultConfigurationSerializer serializer = new DefaultConfigurationSerializer();
        try {
            String xmlAsString = serializer.serialize(jobDetailsConf);
            List jobDetails = (List) xstream.fromXML(xmlAsString);
            jobDetailsMap = new HashMap(jobDetails.size());
            for (Iterator i = jobDetails.iterator(); i.hasNext();) {
                JobDetail jobDetail = (JobDetail) i.next();
                jobDetailsMap.put(jobDetail.getFullName(), jobDetail);
            }
            xmlAsString = serializer.serialize(triggersConf);
            List triggers = (List) xstream.fromXML(xmlAsString);
            triggersMap = new HashMap(triggers.size());
            for (Iterator i = triggers.iterator(); i.hasNext();) {
                Trigger trigger = (Trigger) i.next();
                triggersMap.put(trigger.getFullName(), trigger);
            }
        } catch (Exception e) {
            throw new ConfigurationException(e.toString());
        }

        //add jobs to scheduler
        for (Iterator iter = jobDetailsMap.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            JobDetail jobDetail = (JobDetail) jobDetailsMap.get(key);
            this.getLogger().debug("Adding job detail [" + jobDetail + "] to scheduler");
            scheduler.addJob(jobDetail, true);
        }

        //add job Listener
        if (globalJobListenerClassName != null) {
            JobListener configuredjobListener = (JobListener) Class.forName(globalJobListenerClassName).newInstance();
            wrapper = new ServiceableJobListenerWrapper(configuredjobListener);
            wrapper.enableLogging(this.getLogger().getChildLogger("ServiceableJobListener"));
            wrapper.service(manager);
            scheduler.addGlobalJobListener(wrapper);
        }

        //schedule any triggers that have a job associated
        for (Iterator iter = triggersMap.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            Trigger trigger = (Trigger) triggersMap.get(key);
            if (trigger instanceof CronTrigger) {
                if (trigger.getJobGroup() != null & trigger.getJobName() != null) {
                	   Trigger t = scheduler.getTrigger(trigger.getName(),trigger.getGroup());
                	   if (t==null){
                    CronTrigger triggerToSchedule = new CronTrigger(trigger.getName(),trigger.getGroup(),trigger.getJobName(),trigger.getJobGroup(),((CronTrigger)trigger).getCronExpression());
                    this.getLogger().debug("Scheduling trigger [" + triggerToSchedule.getFullName() + "] for  job ["
                            + triggerToSchedule.getFullJobName() + "] using cron " + triggerToSchedule.getCronExpression());

                    triggerToSchedule.setDescription(trigger.getDescription());
                   // CronTrigger cronTrigger = new CronTrigger("someTriggerCron", Scheduler.DEFAULT_GROUP,
                   //         "simpleJob","DEFAULT_GROUP" ,"* * * * * ?");
                    scheduler.scheduleJob(triggerToSchedule);
                	   }
                 /*   Trigger rightNow = new SimpleTrigger("someTrigger", Scheduler.DEFAULT_GROUP,
                            "notSoSimpleJob","DEFAULT_GROUP" ,new Date(), null, 0,0L);

                    scheduler.scheduleJob(rightNow);*/
                }
            }
        }

        scheduler.start();
        this.getLogger().debug("Quartz scheduler started !");
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            this.getLogger().warn("Problem shutting down scheduler ", e);
        }
        if(wrapper != null) {
            wrapper.dispose();
        }
        scheduler = null;
        manager = null;
    }

    /**
     * @see org.apache.fulcrum.quartz.QuartzScheduler#getScheduler()
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * @see org.apache.fulcrum.quartz.QuartzScheduler#getJobDetailsMap()
     */
    public Map getJobDetailsMap() {
        return jobDetailsMap;
    }

    /*
     * (non-Javadoc)
     *
     * @see br.com.ibnetwork.xingu.quartzscheduler.QuartzScheduler#getTriggersMap()
     */
    public Map getTriggersMap() {
        return triggersMap;
    }

}
