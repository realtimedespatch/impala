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

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import classes.HostBean;
import interfaces.EntryService;

/**
 * Moved to annotation based controller due to retirement of MultiActionController in Spring v 5
 * This class has not been tested but intended as an example.
 */
@Controller
public class TestController {

    private EntryService entryService;

    private HostBean hostBean;
    
    public ModelAndView test(HttpServletRequest request, HttpServletResponse response) {
        
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("staticparam", "1147");
        map.put("hostmessage", hostBean.getMessage());
        map.put("dynamicparam", "" + entryService.getEntriesOfCount(1996).size());

        ModelAndView mav = new ModelAndView("test", map);
        return mav;
    }

    public void setEntryService(EntryService entryService) {
        this.entryService = entryService;
    }
    
    public void setHostBean(HostBean hostBean) {
        this.hostBean = hostBean;
    }

}
