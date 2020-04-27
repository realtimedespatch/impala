/*
 * Copyright 2007-2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.impalaframework.command.framework;

import org.springframework.util.Assert;

/**
 * Contains information on a single property required for executing a command
 * 
 * @author Phil Zoio
 */
public class CommandInfo {

    private String propertyName;

    private String description;

    private String requestString;

    private String defaultValue;

    private String[] extraLines;

    private boolean shared;

    private boolean optional;

    private boolean isolated;

    private boolean globalOverride;

    /**
     * Constructor for <code>CommandInfo</code>
     * @param propertyName the name of the property being extracted
     * @param description a description of the property being extracted
     * @param requestString the String used to prompt the user
     * @param defaultValue the defualt value for the property. Only used if the
     * property cannot be captured from user input, and if there is no property
     * value held in <code>GlobalCommandState</code>
     * @param extraLines extra lines used to prompt the user
     * @param shared if true, the captured value will be added to the
     * <code>GlobalCommandState</code> for subsequent use
     * @param optional if true, then a null or empty value is allowed - you
     * won't be continually prompted until you come up with a value
	 * @param isolated if true, then the value held in
	 * <code>GlobalCommandState</code> will not be used, even if it is
	 * available
	 * @param globalOverride if true, and a global value is present, then the
	 * global value will be used instead of asking the user to provide a value.
	 * Otherwise, the user will be asked for a value, and the global value would
	 * only be used if the user failed to provide a value
	 */
	public CommandInfo(String propertyName, String description, String requestString, String defaultValue,
			String[] extraLines, boolean shared, boolean optional, boolean isolated, boolean globalOverride) {
		super();
		this.propertyName = propertyName;
		this.description = description;
		this.requestString = requestString;
		this.defaultValue = defaultValue;
		this.extraLines = extraLines;
		this.shared = shared;
		this.optional = optional;
		this.isolated = isolated;
		this.globalOverride = globalOverride;
	}

	public String getDescription() {
		return description;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public String getRequestString() {
		return requestString;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String[] getExtraLines() {
		return extraLines;
	}

	public boolean isShared() {
		return shared;
	}

	public boolean isOptional() {
		return optional;
	}

	public boolean isIsolated() {
		return isolated;
	}

	public boolean isGlobalOverride() {
		return globalOverride;
	}

	public String validate(String input) {
		// note that input will not be called with a non-null, untrimmed value
		Assert.notNull(input, "input cannot be null");
		return null;
	}

}
