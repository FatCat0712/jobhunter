package vn.hoidanit.jobhunter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.Skill;
import vn.hoidanit.jobhunter.domain.Subscriber;
import vn.hoidanit.jobhunter.domain.response.email.ResEmailJob;
import vn.hoidanit.jobhunter.repository.JobRepository;
import vn.hoidanit.jobhunter.repository.SkillRepository;
import vn.hoidanit.jobhunter.repository.SubscriberRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubscriberService {
    private final SubscriberRepository subscriberRepository;
    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;
    private final EmailService emailService;

    @Autowired
    public SubscriberService(SubscriberRepository subscriberRepository, SkillRepository skillRepository, JobRepository jobRepository, EmailService emailService) {
        this.subscriberRepository = subscriberRepository;
        this.skillRepository = skillRepository;
        this.jobRepository = jobRepository;
        this.emailService = emailService;
    }

//    @Scheduled(cron = "*/10 * * * * *")
//    public void testCron() {
//        System.out.println(">>> TEST CRON");
//    }

    public boolean existsByEmail(String email) {
        return subscriberRepository.existsByEmail(email);
    }

    public boolean existsById(Long id) {
        return subscriberRepository.existsById(id);
    }

    public Subscriber fetchSubscriberById(Long id) {
        Optional<Subscriber> subscriber = subscriberRepository.findById(id);
        return subscriber.orElse(null);
    }

    public Subscriber fetchByEmail(String email) {
        return subscriberRepository.findByEmail(email);
    }

    public Subscriber handleCreateSubscriber(Subscriber newSubscriber) {
        List<Long> skillIds = newSubscriber.getSkills().stream().map(Skill::getId).toList();

        List<Skill> skillsInDB = skillRepository.findByIdIn(skillIds);

        newSubscriber.setSkills(skillsInDB);

        return subscriberRepository.save(newSubscriber);
    }

    public Subscriber handleUpdateSubscriber(Subscriber updateSubscriber) {
        Subscriber subscriberInDB = fetchSubscriberById(updateSubscriber.getId());

        List<Long> skillIds = updateSubscriber.getSkills().stream().map(Skill::getId).toList();

        List<Skill> skillsInDB = skillRepository.findByIdIn(skillIds);

        subscriberInDB.setSkills(skillsInDB);

        return subscriberRepository.save(subscriberInDB);
    }


    public void sendSubscribersEmailJobs() {
        List<Subscriber> listSubs = subscriberRepository.findAll();
        if(!listSubs.isEmpty()) {
            for(Subscriber sub : listSubs) {
               List<Skill> listSkills =  sub.getSkills();
               if(!listSkills.isEmpty()) {
                   List<Job> listJobs = jobRepository.findBySkillsIn(listSkills);
                   if(!listJobs.isEmpty()) {
                        List<ResEmailJob> arr = listJobs.stream().map(this::convertJobToSendEmail).collect(Collectors.toList());
                       emailService.sendEmailFromTemplateSync(
                               sub.getEmail(),
                               "Cơ hội việc làm hot đang chờ đón bạn, khám phá ngay",
                               "job",
                               sub.getName(),
                               arr);
                   }
               }
            }
        }
    }

    public ResEmailJob convertJobToSendEmail(Job job) {
        ResEmailJob res = new ResEmailJob();
        res.setName(job.getName());
        res.setSalary(job.getSalary());
        res.setCompany(new ResEmailJob.CompanyEmail(job.getCompany().getName()));
        List<Skill> skills = job.getSkills();
        List<ResEmailJob.SkillEmail> s = skills.stream().map(skill -> new ResEmailJob.SkillEmail(skill.getName())).toList();

        res.setSkills(s);
        return res;
    }



}
