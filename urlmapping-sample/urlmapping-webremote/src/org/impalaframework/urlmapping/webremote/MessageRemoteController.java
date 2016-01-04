package org.impalaframework.urlmapping.webremote;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.impalaframework.urlmapping.root.MessageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@SuppressWarnings("all")
public class MessageRemoteController {
    
    private MessageService messageService;
    
    @RequestMapping("/viewMessage.xml")
    public String viewMessage(Map model,
            HttpServletResponse response) {
        
        response.setContentType("application/xml");
        model.put("message", messageService.getMessage());
        
        //need to explicitly return view name as response argument has been used
        return "viewMessage";
    }
    
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
    
}
