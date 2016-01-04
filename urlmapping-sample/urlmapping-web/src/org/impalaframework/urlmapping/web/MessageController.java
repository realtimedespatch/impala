package org.impalaframework.urlmapping.web;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@SuppressWarnings("rawtypes")
public class MessageController {
    
    @RequestMapping("/notes.htm")
    public void notes(Map model) {
    }    
    
    @RequestMapping("/intro.htm")
    public void intro(Map model) {
    }
}
