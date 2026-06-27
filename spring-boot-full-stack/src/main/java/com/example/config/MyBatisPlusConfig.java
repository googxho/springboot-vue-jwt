package com.example.config;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * MyBatis-Plus 手动配置。
 * <p>
 * 由于 MyBatis-Plus 3.5.16 的自动配置与 Spring Boot 4.1.0 不完全兼容
 * （DataSourceAutoConfiguration 包路径变更导致 AutoConfigureAfter 失效，
 * 且 @ConditionalOnSingleCandidate 在自动配置元数据过滤阶段找不到 DataSource），
 * 因此手动声明 SqlSessionFactory / SqlSessionTemplate / MapperScanner 等 Bean。
 * <p>
 * DataSource 由 Spring Boot 的 DataSourceAutoConfiguration 自动创建，
 * 并通过 DataSourceProperties 正确映射 spring.datasource.url → jdbcUrl。
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus SqlSessionFactory。
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        // 设置 MyBatis-Plus 全局配置
        com.baomidou.mybatisplus.core.config.GlobalConfig globalConfig = new com.baomidou.mybatisplus.core.config.GlobalConfig();
        globalConfig.setDbConfig(new com.baomidou.mybatisplus.core.config.GlobalConfig.DbConfig());
        globalConfig.getDbConfig().setIdType(com.baomidou.mybatisplus.annotation.IdType.AUTO);
        factoryBean.setGlobalConfig(globalConfig);
        return factoryBean.getObject();
    }

    /**
     * SqlSessionTemplate。
     */
    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /**
     * Mapper 扫描器 —— 替代 @MapperScan 注解。
     */
    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer configurer = new MapperScannerConfigurer();
        configurer.setBasePackage("com.example.mapper");
        configurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        return configurer;
    }
}
