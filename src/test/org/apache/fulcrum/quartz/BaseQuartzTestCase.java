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
import org.apache.fulcrum.testcontainer.BaseUnitTest;

/**
 * Handle looking up and then the icky cleanup of Quartz.
 *
 * @author <a href="mailto:epughNOSPAM@opensourceconnections.com">Eric Pugh </a>
 */
public abstract class BaseQuartzTestCase extends BaseUnitTest {

    protected QuartzScheduler quartz;

    public BaseQuartzTestCase(String arg0) {
        super(arg0);
    }

    public BaseQuartzTestCase() {
        super("");
    }


    public void setUp() throws Exception {
        SimpleJob.reset();
        NotSoSimpleJob.reset();
        quartz = (QuartzScheduler) lookup(QuartzScheduler.class.getName());
        System.out.println(">>> " + getName() + ">>>");
    }


    public void tearDown() {
        release(QuartzScheduler.class.getName());
        SimpleJob.reset();
        NotSoSimpleJob.reset();
        super.tearDown();
    }
}
