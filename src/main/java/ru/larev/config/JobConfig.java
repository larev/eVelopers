package ru.larev.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import ru.larev.bean.ProcessedUser;
import ru.larev.bean.User;
import ru.larev.repository.ProcessedUserRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * @author Larev Pavel
 * @author http://telegram.me/larev
 */
@Configuration
public class JobConfig {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobBuilderFactory jobs;
    @Autowired
    private StepBuilderFactory steps;
    @Autowired
    private ProcessedUserRepository processedUserRepository;
    @Value("classpath:userdata.xml")
    private Resource file;

    @Bean(name = "asyncJobLauncher")
    public JobLauncher jobLauncher() {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        jobLauncher.setTaskExecutor(simpleAsyncTaskExecutor);
        return jobLauncher;
    }

    @Bean
    public Step step(ItemReader<User> itemReader, ItemProcessor<User, ProcessedUser> itemProcessor, ItemWriter<ProcessedUser> itemWriter) {
        return steps.get("step")
                .<User, ProcessedUser>chunk(1)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean
    public Job job(Step step) {
        return jobs.get("job")
                .incrementer(new RunIdIncrementer())
                .start(step).build();
    }

    @Bean
    @StepScope
    public ItemStreamReader<User> itemReader(Unmarshaller unmarshaller) {
        System.out.println(file);
        StaxEventItemReader<User> userDataStaxEventItemReader = new StaxEventItemReader<>();
        userDataStaxEventItemReader.setResource(file);
        userDataStaxEventItemReader.setUnmarshaller(unmarshaller);
        userDataStaxEventItemReader.setFragmentRootElementName("user");
        return userDataStaxEventItemReader;
    }

    @Bean
    public Unmarshaller unmarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(User.class);
        return marshaller;
    }

    @Bean
    @StepScope
    public ItemProcessor<User, ProcessedUser> itemProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
        return user -> {
            TimeUnit.SECONDS.sleep(10);
            if (user.getBirthday().getMonthValue() == stepExecution.getJobParameters().getLong("month") && user.getBirthday().isBefore(LocalDate.now())) {
                ProcessedUser processedUser = new ProcessedUser();
                processedUser.setJobId(stepExecution.getJobExecutionId());
                processedUser.setName(user.getName());
                processedUser.setDaysBefore((int) user.getBirthday().until(user.getBirthday().plus(1, ChronoUnit.YEARS), ChronoUnit.DAYS));
                LocalDate tempDate = LocalDate.now().withYear(user.getBirthday().getYear());
                if (tempDate.isBefore(user.getBirthday())) {
                    processedUser.setDaysBefore((int) tempDate.until(user.getBirthday(), ChronoUnit.DAYS));
                } else {
                    processedUser.setDaysBefore((int) tempDate.until(user.getBirthday().plus(1, ChronoUnit.YEARS), ChronoUnit.DAYS));
                }
                return processedUser;
            }
            return null;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<ProcessedUser> itemWriter() {
        return list -> processedUserRepository.save(list);
    }
}
