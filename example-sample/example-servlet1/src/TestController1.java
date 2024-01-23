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
import interfaces.MessageService;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.impalaframework.config.FloatPropertyValue;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import classes.ConcreteService;
import classes.MainModuleService;

import shared.ValueHolder;

/**
 * Moved to annotation based controller due to retirement of MultiActionController in Spring v 5
 * This class has not been tested but intended as an example.
 */
@Controller
public class TestController1 {

    private ConcreteService concreteService;
    
    private MainModuleService mainModuleService;
    
    private MessageService missingService;
    
    private EntryService entryService;
    
    private FloatPropertyValue magicNumber;

    @RequestMapping("/test")
    public ModelAndView test(HttpServletRequest request, HttpServletResponse response) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("staticparam", ""+magicNumber.getValue());
        map.put("dynamicparam", "" + entryService.getEntriesOfCount(1996).size());
        
        mainModuleService.doSomething();
        concreteService.doSomething();
        System.out.println(missingService.getMessage());

        setSessionValue(request, map, "shared:intvalue", "shared_intvalue");
        setSessionValue(request, map, "intvalue", "intvalue");

        ModelAndView mav = new ModelAndView("test", map);
        return mav;
    }

    private void setSessionValue(HttpServletRequest request,
            HashMap<String, String> map, 
            final String sessionAttributeName,
            final String moduleAttributeName) {
        
        HttpSession session = request.getSession();
        ValueHolder valueHolder = null;
        
        try {
            //note that a ClassCastException will be thrown here because we are not using session
            //value protection, as this is routed via ExternalModuleServlet
            valueHolder = (ValueHolder) session.getAttribute(sessionAttributeName);
        }
        catch (ClassCastException e) {
            e.printStackTrace();
        }

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

    public void setMagicNumber(FloatPropertyValue magicNumber) {
        this.magicNumber = magicNumber;
    }
    
    public void setConcreteService(ConcreteService concreteService) {
        this.concreteService = concreteService;
    }
    
    public void setMainModuleService(MainModuleService mainModuleService) {
        this.mainModuleService = mainModuleService;
    }
    
    public void setMissingService(MessageService missingService) {
        this.missingService = missingService;
    }

}
