package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials(@Value("${vcap.services}") String vcapServices) {
        return new DatabaseServiceCredentials(vcapServices);
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql", "cleardb"));

        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDataSource(dataSource);

        return hikariDataSource;
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql", "cleardb"));

        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDataSource(dataSource);

        return hikariDataSource;
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5InnoDBDialect");
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        return hibernateJpaVendorAdapter;

    }

    @Bean
    public LocalContainerEntityManagerFactoryBean moviesEntityManagerFactory(DataSource moviesDataSource, HibernateJpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(moviesDataSource);
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPackagesToScan("org.superbiz.moviefun.movies");
        factoryBean.setPersistenceUnitName("movies");
        return factoryBean;
    }

    @Bean
    public PlatformTransactionManager moviesPlatformTransactionManager(@Qualifier("moviesEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public TransactionOperations moviesTransactionOperations(@Qualifier("moviesPlatformTransactionManager") PlatformTransactionManager platformTransactionManager) {
        return new TransactionTemplate(platformTransactionManager);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumsEntityManagerFactory(DataSource albumsDataSource, HibernateJpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(albumsDataSource);
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPackagesToScan("org.superbiz.moviefun.albums");
        factoryBean.setPersistenceUnitName("albums");
        return factoryBean;
    }

    @Bean
    public PlatformTransactionManager albumsPlatformTransactionManager(@Qualifier("albumsEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public TransactionOperations albumsTransactionOperations(@Qualifier("albumsPlatformTransactionManager") PlatformTransactionManager platformTransactionManager) {
        return new TransactionTemplate(platformTransactionManager);
    }
}
