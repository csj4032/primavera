package com.genius.primavera;

import com.genius.primavera.domain.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Slf4j
@EnableBatchProcessing
@SpringBootApplication
public class PrimaveraApplication {
	public static void main(String[] args) {
		System.out.println(Integer.valueOf(200) == Integer.valueOf(200));
		SpringApplication springApplication = new SpringApplicationBuilder(PrimaveraApplication.class)
				.initializers((GenericApplicationContext applicationContext) -> {
					applicationContext.registerBean("jobExecutionListenerSupport", JobExecutionListenerSupport.class, () -> new JobExecutionListenerSupport() {
						@Autowired
						private JdbcTemplate jdbcTemplate;

						@Override
						public void afterJob(JobExecution jobExecution) {
							if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
								jdbcTemplate.query("SELECT FIRST_NAME, LAST_NAME FROM PEOPLE", (rs, row) -> new Person(rs.getString(1), rs.getString(2))
								).forEach(person -> log.info("Found <" + person + "> in the database."));
							}
						}
					});

					applicationContext.registerBean("writer", JdbcBatchItemWriter.class, () ->
							new JdbcBatchItemWriterBuilder<Person>()
									.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
									.sql("INSERT INTO PEOPLE (FIRST_NAME, LAST_NAME) VALUES (:firstName, :lastName)")
									.dataSource(applicationContext.getBean(DataSource.class))
									.build());

					applicationContext.registerBean("reader", FlatFileItemReader.class, () -> new FlatFileItemReaderBuilder<Person>()
							.name("personItemReader")
							.resource(new ClassPathResource("sample-data.csv"))
							.delimited()
							.names(new String[]{"firstName", "lastName"})
							.fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
								setTargetType(Person.class);
							}})
							.build());

					applicationContext.registerBean("processor", ItemProcessor.class, () -> (ItemProcessor<Person, Person>) person -> {
						final String firstName = person.getFirstName().toUpperCase();
						final String lastName = person.getLastName().toUpperCase();
						final Person transformedPerson = new Person(firstName, lastName);
						log.info("Converting (" + person + ") into (" + transformedPerson + ")");
						return transformedPerson;
					});

					applicationContext.registerBean("step1", Step.class, () -> applicationContext.getBean(StepBuilderFactory.class).get("step1")
							.<Person, Person>chunk(10)
							.reader(applicationContext.getBean(FlatFileItemReader.class))
							.processor(applicationContext.getBean(ItemProcessor.class))
							.writer(applicationContext.getBean(JdbcBatchItemWriter.class))
							.build());

					applicationContext.registerBean("importUserJob", Job.class, () -> applicationContext.getBean(JobBuilderFactory.class).get("importUserJob")
							.incrementer(new RunIdIncrementer())
							.listener(applicationContext.getBean(JobExecutionListenerSupport.class))
							.flow(applicationContext.getBean(Step.class))
							.end()
							.build());
				})
				.build();

		System.exit(SpringApplication.exit(springApplication.run(args)));
	}
}