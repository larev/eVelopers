package ru.larev.controller;

import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.larev.bean.ProcessedUser;
import ru.larev.exception.JobFailedException;
import ru.larev.exception.JobNotComplitedException;
import ru.larev.exception.JobNotFoundException;
import ru.larev.payload.JobCreated;
import ru.larev.service.JobService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * @author Larev Pavel
 * @author http://telegram.me/larev
 */
@RestController
public class JobController {

    @Autowired
    private JobService jobService;

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public JobCreated createJob(Integer month) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        month = Optional.ofNullable(month).orElse(LocalDate.now().getMonthValue());
        return new JobCreated(jobService.createJob(month));
    }

    @RequestMapping(value = "/job/{id}", method = RequestMethod.GET)
    public List<ProcessedUser> job(@PathVariable long id) throws JobNotComplitedException, JobNotFoundException, JobFailedException {
        return jobService.getJobResult(id);
    }
}
