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
package org.impalaframework.command.basic;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.impalaframework.command.framework.Command;
import org.impalaframework.command.framework.CommandDefinition;
import org.impalaframework.command.framework.CommandInfo;
import org.impalaframework.command.framework.CommandPropertyValue;
import org.impalaframework.command.framework.CommandState;
import org.impalaframework.file.FileRecurser;
import org.impalaframework.file.RootPathAwareFileFilter;
import org.impalaframework.file.handler.BaseFileRecurseHandler;
import org.impalaframework.file.handler.DefaultClassFilter;
import org.springframework.util.Assert;

/**
 * This class has the job of finding a set of classes which match the supplied
 * search pattern. 
 * @author Phil Zoio
 */
public class ClassFindCommand implements Command {

    public static final String FOUND_CLASSES = ClassFindCommand.class.getName() + "FOUND_CLASSES";

    protected static final int MIN_INPUT_LENGTH = 3;

    private List<File> classDirectories;

    private RootPathAwareFileFilter directoryFilter;

    private List<String> foundClasses;

    public boolean execute(CommandState commandState) {

        Assert.notNull(classDirectories, "classDirectories cannot be null");

        Map<String, CommandPropertyValue> properties = commandState.getProperties();
        CommandPropertyValue classHolder = properties.get("class");

        String searchText = classHolder.getValue();

        ClassFindFileRecurseHandler handler = new ClassFindFileRecurseHandler(searchText);
        FileRecurser recurser = new FileRecurser();

        for (File directory : classDirectories) {
            getDirectoryFilter().setRootPath(directory);
            handler.setRootPath(directory.getAbsolutePath());
            recurser.recurse(handler, directory);
        }

        foundClasses = handler.foundFiles;
        return true;
    }

    public CommandDefinition getCommandDefinition() {
        CommandInfo commandInfo = new CommandInfo("class", "Type (class or interface)", "Please specify class search text",
                null, null, true, false, false, false) {

            @Override
            public String validate(String input) {
                String superValidate = super.validate(input);

                if (superValidate != null) {
                    return "Please enter type (class or interface) to find";
                }
                input = input.trim();

                if (input.length() < MIN_INPUT_LENGTH) {
                    return "Search text should be at least " + MIN_INPUT_LENGTH + " characters long";
                }

                return null;
            }

        };

        CommandDefinition commandDefinition = new CommandDefinition();
        commandDefinition.add(commandInfo);
        return commandDefinition;
    }

    /* **************** setters **************** */

    public void setClassDirectories(List<File> classDirectories) {
        this.classDirectories = classDirectories;
    }

    public void setDirectoryFilter(RootPathAwareFileFilter directoryFilter) {
        this.directoryFilter = directoryFilter;
    }

    /* **************** getters **************** */

    public List<String> getFoundClasses() {
        return foundClasses;
    }

    protected RootPathAwareFileFilter getDirectoryFilter() {
        if (directoryFilter == null) {
            directoryFilter = new DefaultClassFilter();
        }
        return directoryFilter;
    }

    class ClassFindFileRecurseHandler extends BaseFileRecurseHandler {
        
        private String packageSegment;

        private String classSegment;

        private String rootPath;

        public ClassFindFileRecurseHandler(String searchText) {
            super();
            Assert.notNull(searchText, "searchText cannot be null");
            Assert.isTrue(searchText.trim().length() >= 3, "searchText length must be at least 3");

            int lastDotIndex = searchText.lastIndexOf('.');
            if (lastDotIndex >= 0) {
                this.classSegment = searchText.substring(lastDotIndex + 1);
                this.packageSegment = searchText.substring(0, lastDotIndex);
            }
            else {
                this.classSegment = searchText;
            }
        }

        public void setRootPath(String rootPath) {
            this.rootPath = rootPath;
        }

        private List<String> foundFiles = new ArrayList<String>();

        public FileFilter getDirectoryFilter() {
            if (directoryFilter != null)
                return directoryFilter;
            return new DefaultClassFilter();
        }

        public void handleFile(File file) {
            ClassFindCommandFilter
                filter = new ClassFindCommandFilter(rootPath, classSegment, packageSegment);
            
            if (filter.accept(file)) {
                foundFiles.add(filter.getClassName());
            }
        }

        @Override
        public void handleDirectory(File directory) {
        }

        String getClassSegment() {
            return classSegment;
        }

        String getPackageSegment() {
            return packageSegment;
        }

    }
}
