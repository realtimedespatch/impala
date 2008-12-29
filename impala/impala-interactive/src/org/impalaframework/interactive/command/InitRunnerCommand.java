/*
 * Copyright 2007-2008 the original author or authors.
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

package org.impalaframework.interactive.command;

import org.impalaframework.command.framework.Command;
import org.impalaframework.command.framework.CommandDefinition;
import org.impalaframework.command.framework.CommandState;
import org.impalaframework.facade.Impala;
import org.impalaframework.facade.FacadeConstants;
import org.impalaframework.facade.ParentReloadingOperationsFacade;

public class InitRunnerCommand implements Command {

	public boolean execute(CommandState commandState) {
		// only set this if not set
		if (System.getProperty(FacadeConstants.FACADE_CLASS_NAME) == null) {
			System.setProperty(FacadeConstants.FACADE_CLASS_NAME, ParentReloadingOperationsFacade.class.getName());
		}
		Impala.init();

		LoadDefinitionFromClassCommand command = new LoadDefinitionFromClassCommand();
		return command.execute(commandState);
	}

	public CommandDefinition getCommandDefinition() {
		return new CommandDefinition();
	}

}