package org.demo.test.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author luoli
 * create on 2119/4/21
 */

@AutoConfigureAfter(CommonConfig.class)
@Configuration
@MapperScan(basePackages = "com.zhongfeng.test.persistence.mapper.ds1", sqlSessionTemplateRef = "db1SqlSessionTemplate")
public class DataSource1Config {

    private PathMatchingResourcePatternResolver resolver;

    private Environment environment;

    public DataSource1Config(Environment environment, PathMatchingResourcePatternResolver resolver) {
        this.environment = environment;
        this.resolver = resolver;
    }

    @Bean(name = "ds1DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.ds1")
    public DataSource ds1DataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "db1TransactionManager")
    public DataSourceTransactionManager db1TransactionManager() {
        return new DataSourceTransactionManager(ds1DataSource());

    }
    @Bean(name = "ds1SqlSessionFactory")
    public SqlSessionFactory ds1SqlSessionFactory() throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(ds1DataSource());
        bean.setConfigLocation( new ClassPathResource(environment.getProperty("mybatis.config-location")));
        bean.setMapperLocations(resolveMapperLocations(environment.getProperty("sharding.jdbc.ds1.mapper-locations").split(",")));
        return bean.getObject();
    }

    public Resource[] resolveMapperLocations(String[] locations) {
        return Stream.of(Optional.ofNullable(locations).orElse(new String[1]))
                .flatMap(location -> Stream.of(getResources(location)))
                .toArray(Resource[]::new);
    }

    private Resource[] getResources(String location) {
        try {
            return resolver.getResources(location);
        } catch (IOException e) {
            return new Resource[0];
        }
    }

    @Bean(name = "db1SqlSessionTemplate")
    public SqlSessionTemplate db1SqlSessionTemplate(@Qualifier("ds1SqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
