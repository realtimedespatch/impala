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

package org.impalaframework.classloader.graph;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

//FIXME this should be replaced by a resource loader, as only the findClassBytes method is being used
public class CustomClassLoader extends org.impalaframework.classloader.FileSystemClassLoader {

	public CustomClassLoader(ClassLoader parent, File[] locations) {
		super(parent, locations);
	}

	public CustomClassLoader(File[] locations) {
		super(locations);
	}

	@Override
	public byte[] findClassBytes(String className) throws IOException {
		return super.findClassBytes(className);
	}
	
	@Override
	public String toString() {
		final String string = super.toString();
		StringBuffer buffer = new StringBuffer(string);
		buffer.append(": Locations: " + Arrays.toString(getLocations()));
		return buffer.toString();
	}
	
}