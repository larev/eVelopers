package ru.larev.repository;

import org.springframework.data.repository.CrudRepository;
import ru.larev.bean.ProcessedUser;

import java.util.List;

/**
 * @author Larev Pavel
 * @author http://telegram.me/larev
 */
public interface ProcessedUserRepository extends CrudRepository<ProcessedUser, Long> {
    List<ProcessedUser> findAllByJobId(long jobId);
}
