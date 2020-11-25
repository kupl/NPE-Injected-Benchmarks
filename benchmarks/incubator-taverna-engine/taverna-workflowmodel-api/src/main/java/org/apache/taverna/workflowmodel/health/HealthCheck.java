/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workflowmodel.health;

import org.apache.taverna.visit.VisitKind;
import org.apache.taverna.visit.Visitor;

/**
 * A HealthCheck is a kind of visit that determines if the corresponding object
 * in a workflow (normally an Activity) will work during a workflow run.
 * 
 * @author alanrw
 * 
 */
public class HealthCheck extends VisitKind {
	/*
	 * The following values indicate the type of results that can be associated
	 * with a VisitReport generated by a health-checking visitor.
	 */

	public static final int NO_PROBLEM = 0;
	public static final int NOT_IMPLEMENTED = 1;
	public static final int CONNECTION_PROBLEM = 2;
	public static final int INVALID_URL = 3;
	public static final int TIME_OUT = 4;
	public static final int IO_PROBLEM = 5;
	public static final int MISSING_CLASS = 6;
	public static final int MISSING_DEPENDENCY = 7;
	public static final int INVALID_SCRIPT = 8;
	public static final int NO_CONFIGURATION = 9;
	public static final int NULL_VALUE = 10;
	public static final int DEFAULT_VALUE = 11;
	public static final int BAD_WSDL = 12;
	public static final int NOT_HTTP = 13;
	public static final int UNSUPPORTED_STYLE = 14;
	public static final int UNKNOWN_OPERATION = 15;
	public static final int NO_ENDPOINTS = 16;
	public static final int INVALID_CONFIGURATION = 17;
	public static final int NULL_DATATYPE = 18;
	public static final int DISABLED = 19;
	public static final int DATATYPE_SOURCE = 20;
	public static final int UNRECOGNIZED = 21;
    public static final int LOOP_CONNECTION = 22;
    public static final int UNMANAGED_LOCATION = 23;
    public static final int INCOMPATIBLE_MIMETYPES = 24;
    public static final int HIGH_PORT_DEPTH = 25;

    @SuppressWarnings("rawtypes")
	private static final Class healthCheckerClass = HealthChecker.class;

    /**
	 * Sub-classes of HealthChecker are used to perform HealthCheck visits.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Visitor<?>> getVisitorClass() {
		return healthCheckerClass;
	}

	private static class Singleton {
		private static HealthCheck instance = new HealthCheck();
	}

	public static HealthCheck getInstance() {
		return Singleton.instance;
	}
}
