package ru.larev.service;

import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.larev.bean.ProcessedUser;
import ru.larev.exception.JobFailedException;
import ru.larev.exception.JobNotComplitedException;
import ru.larev.exception.JobNotFoundException;
import ru.larev.repository.ProcessedUserRepository;

import java.util.List;

/**
 * @author Larev Pavel
 * @author http://telegram.me/larev
 */
@Service
public class BatchJobService implements JobService {
    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    @Qualifier("asyncJobLauncher")
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;
    @Autowired
    private ProcessedUserRepository processedUserRepository;

    @Override
    public long createJob(int month) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters jobParameters = new JobParametersBuilder().addLong("month", (long) month).addLong("time", System.currentTimeMillis()).toJobParameters();
        JobExecution run = jobLauncher.run(job, jobParameters);
        return run.getJobId();
    }

    @Override
    public List<ProcessedUser> getJobResult(long jobId) throws JobNotComplitedException, JobNotFoundException, JobFailedException {
        JobExecution jobExecution = jobExplorer.getJobExecution(jobId);
        if (jobExecution == null) throw new JobNotFoundException();
        if (jobExecution.getStatus() == BatchStatus.FAILED) throw new JobFailedException();
        if (jobExecution.getStatus() != BatchStatus.COMPLETED) throw new JobNotComplitedException();
        return processedUserRepository.findAllByJobId(jobId);
    }
}
