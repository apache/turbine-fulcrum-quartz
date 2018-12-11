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


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.avalon.framework.logger.Log4JLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.fulcrum.quartz.test.NotSoSimpleJob;
import org.apache.fulcrum.quartz.test.SimpleJob;
import org.apache.fulcrum.testcontainer.BaseUnit5Test;
import org.apache.log4j.LogManager;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * Handle looking up and then the icky cleanup of Quartz.
 *
 * @author <a href="mailto:epughNOSPAM@opensourceconnections.com">Eric Pugh </a>
 */
@RunWith(JUnitPlatform.class)
public class BaseQuartzTestCase extends BaseUnit5Test {

    private final String preDefinedOutput = "{\"container\":{\"cf\":\"Config.xml\"},\"configurationName\":\"Config.xml\",\"name\":\"mytest\"}";
    QuartzScheduler quartz = null;
    Logger logger;

    @BeforeEach
    public void setUp() throws Exception {
        logger = new Log4JLogger(LogManager.getLogger(getClass().getName()) );
		SimpleJob.reset();
		NotSoSimpleJob.reset();
        try {
        	quartz = (QuartzScheduler) this.lookup(QuartzScheduler.ROLE);
        } catch (Throwable e) {
            fail(e.getMessage());
        }
        assertNotNull(quartz);
    }	
	
	
	/* (non-Javadoc)
	 * @see org.apache.fulcrum.testcontainer.BaseUnit5Test#tearDown()
	 */
	@After
	public void tearDown() {
		release(QuartzScheduler.ROLE);
		SimpleJob.reset();
		NotSoSimpleJob.reset();
		super.tearDown();
	}
}
