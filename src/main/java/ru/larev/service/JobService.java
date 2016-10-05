package ru.larev.service;

import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import ru.larev.bean.ProcessedUser;
import ru.larev.exception.JobFailedException;
import ru.larev.exception.JobNotComplitedException;
import ru.larev.exception.JobNotFoundException;

import java.util.List;

/**
 * @author Larev Pavel
 * @author http://telegram.me/larev
 */
public interface JobService {
    long createJob(int month) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException;

    List<ProcessedUser> getJobResult(long jobId) throws JobNotComplitedException, JobNotFoundException, JobFailedException;
}
