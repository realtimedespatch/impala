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

package org.impalaframework.web.servlet.invoker;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletInvokerUtils {
	
	public static void invoke(Object target, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if (target instanceof HttpServiceInvoker) {
			HttpServiceInvoker invoker = (HttpServiceInvoker) target;
			invoker.invoke(request, response);
		} else if (target instanceof HttpServlet) {
			HttpServlet servlet = (HttpServlet) target;
			servlet.service(request, response);
		}
		
	}
}
