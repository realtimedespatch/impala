package org.impalaframework.urlmapping.webadmin;

import java.util.Map;

import org.impalaframework.urlmapping.root.UpdatableMessageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@SuppressWarnings("all")
public class MessageAdminController {
    
    private UpdatableMessageService messageService;
    
    @RequestMapping("/viewMessage.htm")
    public void viewMessage(Map model,
            @RequestParam(value="result", required=false) String result) {
        model.put("message", messageService.getMessage());
        model.put("result", result);
    }
    
    @RequestMapping("/updateMessage.htm")
    public String updateMessage(
            @RequestParam("message") String message) {
        messageService.setMessage(message);
        return "redirect:viewMessage.htm?result=Updated message value to '" + message + "'";
    }

    public void setMessageService(UpdatableMessageService messageService) {
        this.messageService = messageService;
    }

}
