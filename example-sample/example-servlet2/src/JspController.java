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

import interfaces.EntryService;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import shared.ValueHolder;
import classes.HostBean;

public class JspController extends MultiActionController implements ServletContextAware {

    private EntryService entryService;
    
    private AutowiredClass autowiredClass;
    
    private HostBean hostBean;

    public ModelAndView jsp(HttpServletRequest request, HttpServletResponse response) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("staticparam", "2222");
        map.put("hostmessage", hostBean.getMessage());
        map.put("dynamicparam", "" + entryService.getEntriesOfCount(1996).size());
        map.put("message", autowiredClass.useMessage());

        setSessionValue(request, map, "shared:intvalue", "shared_intvalue");
        setSessionValue(request, map, "intvalue", "intvalue");        
        
        ModelAndView mav = new ModelAndView("/servlet2/somejsp", map);
        return mav;
    }

    private void setSessionValue(HttpServletRequest request,
            HashMap<String, String> map, 
            final String sessionAttributeName,
            final String moduleAttributeName) {
        
        HttpSession session = request.getSession();
        ValueHolder valueHolder = (ValueHolder) session.getAttribute(sessionAttributeName);
        if (valueHolder == null) {
            valueHolder = new ValueHolder();
            session.setAttribute(sessionAttributeName, valueHolder);
        }
        valueHolder.increment();
        map.put(moduleAttributeName, ""+valueHolder.getCount());
    }
    
    public void setEntryService(EntryService entryService) {
        this.entryService = entryService;
    }
    
    public void setAutowiredClass(AutowiredClass autowiredClass) {
        this.autowiredClass = autowiredClass;
    }
    
    public void setHostBean(HostBean hostBean) {
        this.hostBean = hostBean;
    }

}
