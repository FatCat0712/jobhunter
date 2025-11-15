package vn.hoidanit.jobhunter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.jobhunter.service.SubscriberService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class EmailController {
    private final SubscriberService subscriberService;

    @Autowired
    public EmailController(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    @ApiMessage("Send simple email")
    @GetMapping("/email")
    public String sendSimpleEmail() {
//        emailService.sendEmailSync("tdemoch0712@gmail.com", "test send email", "<h1><b>Hello</b></h1>", false,true);
//        emailService.sendEmailFromTemplateSync("tdemoch0712@gmail.com", "test send email", "job");
        subscriberService.sendSubscribersEmailJobs();
        return "ok";
    }
}
