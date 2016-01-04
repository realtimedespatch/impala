package org.impalaframework.urlmapping.webview;

import java.util.Map;

import org.impalaframework.urlmapping.root.MessageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@SuppressWarnings("all")
public class MessageViewController {
    
    private MessageService messageService;
    
    @RequestMapping("/viewMessage.htm")
    public void viewMessage(Map model) {
        model.put("message", messageService.getMessage());
    }
    
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

}
