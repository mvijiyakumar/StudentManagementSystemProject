package com.studentmanagementsystem.api.configuration;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
@Configuration
@ComponentScan(basePackages = "com.studentmanagementsystem.api")
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.studentmanagementsystem.api.repository")

public class JpaConfiguration {
	 @Bean
	    public DataSource dataSource() {
	        DriverManagerDataSource ds = new DriverManagerDataSource();
	        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
	        ds.setUrl("jdbc:mysql://humworld-humhealth-private-db-rds-dev.cvmya86ssv13.us-east-2.rds.amazonaws.com/student_management_system_project");
	        ds.setUsername("vijiyakumar");
	        ds.setPassword("HumVijiyakumar@2026");
	        return ds;
	    }

	    @Bean
	    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
	        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
	        emf.setDataSource(dataSource());
	        emf.setPackagesToScan("com.studentmanagementsystem.api.model.entity");
	        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
	        Properties jpaProps = new Properties();
	        jpaProps.setProperty("hibernate.hbm2ddl.auto", "none");
	        jpaProps.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
	        jpaProps.setProperty("hibernate.show_sql", "true");

	        emf.setJpaProperties(jpaProps);
	        return emf;
	    }

	    @Bean
	    public JpaTransactionManager transactionManager() {
	        JpaTransactionManager txManager = new JpaTransactionManager();
	        txManager.setEntityManagerFactory(entityManagerFactory().getObject());
	        return txManager;
	    }

	    
	    @Bean
	    public JavaMailSender mailSender() {
	        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	        mailSender.setHost("smtp.gmail.com");
	        mailSender.setPort(587);

	        mailSender.setUsername("vijayakumar2042003@gmail.com"); // change
	        mailSender.setPassword("awgkkbhqykikrcog");   // app password

	        Properties props = mailSender.getJavaMailProperties();
	        props.put("mail.transport.protocol", "smtp");
	        props.put("mail.smtp.auth", "true");
	        props.put("mail.smtp.starttls.enable", "true");
	        props.put("mail.debug", "true");

	        return mailSender;
	    }

}
