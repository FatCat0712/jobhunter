package vn.hoidanit.jobhunter.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.domain.Subscriber;
import vn.hoidanit.jobhunter.service.SubscriberService;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class SubscriberController {
    private final SubscriberService subscriberService;

    @Autowired
    public SubscriberController(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    @PostMapping("/subscribers")
    public ResponseEntity<?> createSubscriber(@Valid @RequestBody Subscriber subscriber) throws IdInvalidException {
         boolean isExist = subscriberService.existsByEmail(subscriber.getEmail());
         if(isExist) {
             throw new IdInvalidException(String.format("Email %s da ton tai", subscriber.getEmail()));
         }
         Subscriber newSubscriber = subscriberService.handleCreateSubscriber(subscriber);

         return ResponseEntity.status(HttpStatus.CREATED).body(newSubscriber);
    }

    @PutMapping("/subscribers")
    public ResponseEntity<?> updateSubscriber(@RequestBody Subscriber subscriber) throws IdInvalidException {
        boolean isExist = subscriberService.existsById(subscriber.getId());
        if(!isExist) {
            throw new IdInvalidException(String.format("Subscriber with id =  %d khong ton tai", subscriber.getId()));
        }
        Subscriber newSubscriber = subscriberService.handleUpdateSubscriber(subscriber);

        return ResponseEntity.status(HttpStatus.CREATED).body(newSubscriber);
    }

    @PostMapping("/subscribers/skills")
    public ResponseEntity<Subscriber> getSubscribersSkill() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        return ResponseEntity.ok().body(subscriberService.fetchByEmail(email));
    }





}
