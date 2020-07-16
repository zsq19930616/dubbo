/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.config.spring.beans.factory.annotation;

import com.alibaba.dubbo.config.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.alibaba.dubbo.config.spring.util.BeanFactoryUtils.getBeans;
import static com.alibaba.dubbo.config.spring.util.BeanFactoryUtils.getOptionalBean;

/**
 * Abstract Configurable {@link Annotation} Bean Builder
 * @since 2.5.7
 */
abstract class AbstractAnnotationConfigBeanBuilder<A extends Annotation, B extends AbstractInterfaceConfig> {

    // 日志
    protected final Log logger = LogFactory.getLog(getClass());

    // 注解
    protected final A annotation;

    // 应用内上下文，知道 spring 就知道这玩意
    protected final ApplicationContext applicationContext;

    // 类加载器
    protected final ClassLoader classLoader;

    // bean 实例
    protected Object bean;

    // 接口类型
    protected Class<?> interfaceClass;

    /**
     * 构造器，初始化 注解，类加载器，spring应用上下文
     * @param annotation
     * @param classLoader
     * @param applicationContext
     */
    protected AbstractAnnotationConfigBeanBuilder(A annotation, ClassLoader classLoader,
                                                  ApplicationContext applicationContext) {
        // 空校验
        Assert.notNull(annotation, "The Annotation must not be null!");
        Assert.notNull(classLoader, "The ClassLoader must not be null!");
        Assert.notNull(applicationContext, "The ApplicationContext must not be null!");
        this.annotation = annotation;
        this.applicationContext = applicationContext;
        this.classLoader = classLoader;

    }

    /**
     * Build {@link B}
     *
     * @return non-null
     * @throws Exception
     */
    public final B build() throws Exception {

        // 检查依赖，现在是空方法
        checkDependencies();

        // 构建,  抽象方法
        B bean = doBuild();

        // 配置bean
        configureBean(bean);

        // 启动info日志
        if (logger.isInfoEnabled()) {
            // bean 已经构建
            logger.info(bean + " has been built.");
        }

        return bean;

    }

    private void checkDependencies() {

    }

    /**
     * Builds {@link B Bean}
     *
     * @return {@link B Bean}
     */
    protected abstract B doBuild();


    protected void configureBean(B bean) throws Exception {

        // 配置bean之前 抽象方法
        preConfigureBean(annotation, bean);

        // 配置注册中心
        configureRegistryConfigs(bean);

        // 配置监控中心
        configureMonitorConfig(bean);

        // 配置应用配置
        configureApplicationConfig(bean);

        // 配置模块
        configureModuleConfig(bean);

        // 配置bean
        postConfigureBean(annotation, bean);

    }

    protected abstract void preConfigureBean(A annotation, B bean) throws Exception;


    private void configureRegistryConfigs(B bean) {

        // 解析注册中心配置,可以有多个
        String[] registryConfigBeanIds = resolveRegistryConfigBeanNames(annotation);
        // 构建注册中心bean
        List<RegistryConfig> registryConfigs = getBeans(applicationContext, registryConfigBeanIds, RegistryConfig.class);
        // 设置注册中心
        bean.setRegistries(registryConfigs);

    }

    private void configureMonitorConfig(B bean) {

        // 获取监控中心名称
        String monitorBeanName = resolveMonitorConfigBeanName(annotation);

        // 生成监控中心配置
        MonitorConfig monitorConfig = getOptionalBean(applicationContext, monitorBeanName, MonitorConfig.class);

        // 设置监控中心
        bean.setMonitor(monitorConfig);

    }

    private void configureApplicationConfig(B bean) {

        // 获取应用名
        String applicationConfigBeanName = resolveApplicationConfigBeanName(annotation);

        // 获取配置类
        ApplicationConfig applicationConfig =
                getOptionalBean(applicationContext, applicationConfigBeanName, ApplicationConfig.class);

        // 设置配置类
        bean.setApplication(applicationConfig);

    }

    private void configureModuleConfig(B bean) {

        // 获取模块名
        String moduleConfigBeanName = resolveModuleConfigBeanName(annotation);

        // 获取模块配置
        ModuleConfig moduleConfig =
                getOptionalBean(applicationContext, moduleConfigBeanName, ModuleConfig.class);

        // 设置模块配置
        bean.setModule(moduleConfig);

    }

    /**
     * Resolves the bean name of {@link ModuleConfig}
     *
     * @param annotation {@link A}
     * @return
     */
    protected abstract String resolveModuleConfigBeanName(A annotation);

    /**
     * Resolves the bean name of {@link ApplicationConfig}
     *
     * @param annotation {@link A}
     * @return
     */
    protected abstract String resolveApplicationConfigBeanName(A annotation);


    /**
     * Resolves the bean ids of {@link com.alibaba.dubbo.config.RegistryConfig}
     *
     * @param annotation {@link A}
     * @return non-empty array
     */
    protected abstract String[] resolveRegistryConfigBeanNames(A annotation);

    /**
     * Resolves the bean name of {@link MonitorConfig}
     *
     * @param annotation {@link A}
     * @return
     */
    protected abstract String resolveMonitorConfigBeanName(A annotation);

    /**
     * Configures Bean
     *
     * @param annotation
     * @param bean
     */
    protected abstract void postConfigureBean(A annotation, B bean) throws Exception;


    public <T extends AbstractAnnotationConfigBeanBuilder<A, B>> T bean(Object bean) {
        this.bean = bean;
        return (T) this;
    }

    public <T extends AbstractAnnotationConfigBeanBuilder<A, B>> T interfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
        return (T) this;
    }

}
