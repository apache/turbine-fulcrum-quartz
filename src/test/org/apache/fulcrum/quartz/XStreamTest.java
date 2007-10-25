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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.fulcrum.quartz.test.NotSoSimpleJob;
import org.apache.fulcrum.quartz.test.SimpleJob;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Verify that XStream works properly.  Also shows us what the XStream output
 * will look like.
 *
 * @author <a href="mailto:epughNOSPAM@opensourceconnections.com">Eric Pugh</a>
 *
 */
public class XStreamTest extends TestCase {

    public XStreamTest(String arg0) {
        super(arg0);
    }

    public void testSavingTwoTriggersWXstream() throws Exception
    {
        List triggers = new ArrayList();
        XStream xstream = new XStream(new DomDriver()); // does not require XPP3 library

        Trigger st = new SimpleTrigger("bob","jones");
        String xml = xstream.toXML(st);
       // System.out.println(xml);
        SimpleTrigger st2 = (SimpleTrigger)xstream.fromXML(xml);
        assertEquals(st,st2);
        triggers.add(st2);

        CronTrigger st3 = new CronTrigger("cron","jones","jobName","jobGroup","0 0 12 * * ?");
        xml = xstream.toXML(st3);
        System.out.println(xml);
        CronTrigger st4 = (CronTrigger)xstream.fromXML(xml);
        assertEquals(st3,st4);
        triggers.add(st4);

        xml = xstream.toXML(triggers);
        //System.out.println(xml);


    }

    public void testSavingTwoJobsWXstream() throws Exception
    {
        List jobs = new ArrayList();
        XStream xstream = new XStream(new DomDriver()); // does not require XPP3 library

        JobDetail st = new JobDetail("bob","jones",SimpleJob.class);
        String xml = xstream.toXML(st);
       // System.out.println(xml);
        JobDetail st2 = (JobDetail)xstream.fromXML(xml);
        assertEquals(st.getFullName(),st2.getFullName());
        jobs.add(st2);

        JobDetail st3 = new JobDetail("bob2","jones2",NotSoSimpleJob.class);
        xml = xstream.toXML(st3);
      //  System.out.println(xml);
        JobDetail st4 = (JobDetail)xstream.fromXML(xml);
        assertEquals(st3.getFullName(),st4.getFullName());
        jobs.add(st4);

        xml = xstream.toXML(jobs);
      //  System.out.println(xml);


    }

}
