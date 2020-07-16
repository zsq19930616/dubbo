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
package com.alibaba.dubbo.config.spring.context.annotation;

import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.DubboConfigBindingBeanPostProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.*;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.alibaba.dubbo.config.spring.util.PropertySourcesUtils.getSubProperties;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.registerWithGeneratedName;

/**
 * {@link AbstractConfig Dubbo Config} binding Bean registrar
 *
 * @see EnableDubboConfigBinding
 * @see DubboConfigBindingBeanPostProcessor
 * @since 2.5.8
 */
// dubbo 配置绑定注册器
public class DubboConfigBindingRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    // 日志
    private final Log log = LogFactory.getLog(getClass());

    // 配置环境
    private ConfigurableEnvironment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获取类或者注解上的 EnableDubboConfigBinding 注解类
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableDubboConfigBinding.class.getName()));
        // 注册为spring bean
        // registry spring bean的注册器
        registerBeanDefinitions(attributes, registry);

    }

    protected void registerBeanDefinitions(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {

        // 获取环境前缀
        String prefix = environment.resolvePlaceholders(attributes.getString("prefix"));

        // 获取配置类
        Class<? extends AbstractConfig> configClass = attributes.getClass("type");

        // 是否注册多实例
        boolean multiple = attributes.getBoolean("multiple");

        // 注册bean
        registerDubboConfigBeans(prefix, configClass, multiple, registry);

    }

    /**
     *
     * @param prefix        环境
     * @param configClass   配置类
     * @param multiple      是否多实例
     * @param registry      注册器
     */
    private void registerDubboConfigBeans(String prefix,
                                          Class<? extends AbstractConfig> configClass,
                                          boolean multiple,
                                          BeanDefinitionRegistry registry) {

        PropertySources propertySources = environment.getPropertySources();

        // env.xx.xx
        Map<String, String> properties = getSubProperties(propertySources, prefix);

        if (CollectionUtils.isEmpty(properties)) {
            if (log.isDebugEnabled()) {
                log.debug("There is no property for binding to dubbo config class [" + configClass.getName()
                        + "] within prefix [" + prefix + "]");
            }
            return;
        }

        // 注册单例还是多实例,返回bean 的名称集合
        Set<String> beanNames = multiple ? resolveMultipleBeanNames(prefix, properties) :
                Collections.singleton(resolveSingleBeanName(configClass, properties, registry));

        for (String beanName : beanNames) {

            registerDubboConfigBean(beanName, configClass, registry);

            MutablePropertyValues propertyValues = resolveBeanPropertyValues(beanName, multiple, properties);

            registerDubboConfigBindingBeanPostProcessor(beanName, propertyValues, registry);

        }

    }

    private void registerDubboConfigBean(String beanName, Class<? extends AbstractConfig> configClass,
                                         BeanDefinitionRegistry registry) {

        BeanDefinitionBuilder builder = rootBeanDefinition(configClass);

        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();

        registry.registerBeanDefinition(beanName, beanDefinition);

        if (log.isInfoEnabled()) {
            log.info("The dubbo config bean definition [name : " + beanName + ", class : " + configClass.getName() +
                    "] has been registered.");
        }

    }

    private void registerDubboConfigBindingBeanPostProcessor(String beanName, PropertyValues propertyValues,
                                                             BeanDefinitionRegistry registry) {

        Class<?> processorClass = DubboConfigBindingBeanPostProcessor.class;

        BeanDefinitionBuilder builder = rootBeanDefinition(processorClass);

        builder.addConstructorArgValue(beanName).addConstructorArgValue(propertyValues);

        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();

        beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        registerWithGeneratedName(beanDefinition, registry);

        if (log.isInfoEnabled()) {
            log.info("The BeanPostProcessor bean definition [" + processorClass.getName()
                    + "] for dubbo config bean [name : " + beanName + "] has been registered.");
        }

    }

    private MutablePropertyValues resolveBeanPropertyValues(String beanName, boolean multiple,
                                                            Map<String, String> properties) {

        MutablePropertyValues propertyValues = new MutablePropertyValues();

        if (multiple) { // For Multiple Beans

            MutablePropertySources propertySources = new MutablePropertySources();
            propertySources.addFirst(new MapPropertySource(beanName, new TreeMap<String, Object>(properties)));

            Map<String, String> subProperties = getSubProperties(propertySources, beanName);

            propertyValues.addPropertyValues(subProperties);


        } else { // For Single Bean

            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String propertyName = entry.getKey();
                if (!propertyName.contains(".")) { // ignore property name with "."
                    propertyValues.addPropertyValue(propertyName, entry.getValue());
                }
            }

        }

        return propertyValues;

    }

    @Override
    public void setEnvironment(Environment environment) {

        Assert.isInstanceOf(ConfigurableEnvironment.class, environment);

        this.environment = (ConfigurableEnvironment) environment;

    }

    private Set<String> resolveMultipleBeanNames(String prefix, Map<String, String> properties) {

        Set<String> beanNames = new LinkedHashSet<String>();

        for (String propertyName : properties.keySet()) {

            int index = propertyName.indexOf(".");

            if (index > 0) {

                String beanName = propertyName.substring(0, index);

                beanNames.add(beanName);
            }

        }

        return beanNames;

    }

    private String resolveSingleBeanName(Class<? extends AbstractConfig> configClass, Map<String, String> properties,
                                         BeanDefinitionRegistry registry) {

        String beanName = properties.get("id");

        if (!StringUtils.hasText(beanName)) {
            BeanDefinitionBuilder builder = rootBeanDefinition(configClass);
            beanName = BeanDefinitionReaderUtils.generateBeanName(builder.getRawBeanDefinition(), registry);
        }

        return beanName;

    }

}
