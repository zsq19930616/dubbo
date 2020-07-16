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

import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import static com.alibaba.dubbo.config.spring.util.BeanFactoryUtils.getOptionalBean;

/**
 * {@link ReferenceBean} Builder
 *
 * @since 2.5.7
 */
// ReferenceBean 构建类
class ReferenceBeanBuilder extends AbstractAnnotationConfigBeanBuilder<Reference, ReferenceBean> {


    private ReferenceBeanBuilder(Reference annotation, ClassLoader classLoader, ApplicationContext applicationContext) {
        super(annotation, classLoader, applicationContext);
    }

    private void configureInterface(Reference reference, ReferenceBean referenceBean) {

        // 获取接口名
        Class<?> interfaceClass = reference.interfaceClass();

        // 没有
        if (void.class.equals(interfaceClass)) {

            interfaceClass = null;

            // 获取接口名
            String interfaceClassName = reference.interfaceName();

            // 不为空
            if (StringUtils.hasText(interfaceClassName)) {
                // 加载类
                if (ClassUtils.isPresent(interfaceClassName, classLoader)) {
                    // 设置类型
                    interfaceClass = ClassUtils.resolveClassName(interfaceClassName, classLoader);
                }
            }

        }

        // 还为空
        if (interfaceClass == null) {
            interfaceClass = this.interfaceClass;
        }

        // 必须标注接口
        Assert.isTrue(interfaceClass.isInterface(),
                "The class of field or method that was annotated @Reference is not an interface!");

        // 设置接口
        referenceBean.setInterface(interfaceClass);

    }


    /**
     * 配置消费者
     * @param reference
     * @param referenceBean
     */
    private void configureConsumerConfig(Reference reference, ReferenceBean<?> referenceBean) {

        // 获取消费者 beanName
        String consumerBeanName = reference.consumer();

        ConsumerConfig consumerConfig = getOptionalBean(applicationContext, consumerBeanName, ConsumerConfig.class);

        // 设置消费者
        referenceBean.setConsumer(consumerConfig);

    }

    @Override
    protected ReferenceBean doBuild() {
        // 创建 ReferenceBean 对象
        return new ReferenceBean<Object>(annotation);
    }

    /**
     * 创建Bean之前
     *
     * @param annotation
     * @param bean
     */
    @Override
    protected void preConfigureBean(Reference annotation, ReferenceBean bean) {
        Assert.notNull(interfaceClass, "The interface class must set first!");
    }

    /**
     * 模块名
     *
     * @param annotation {@link A}
     * @return
     */
    @Override
    protected String resolveModuleConfigBeanName(Reference annotation) {
        return annotation.module();
    }

    /**
     * 应用名
     *
     * @param annotation {@link A}
     * @return
     */
    @Override
    protected String resolveApplicationConfigBeanName(Reference annotation) {
        return annotation.application();
    }

    /**
     * 注册中心
     *
     * @param annotation {@link A}
     * @return
     */
    @Override
    protected String[] resolveRegistryConfigBeanNames(Reference annotation) {
        return annotation.registry();
    }

    /**
     * 监控中心名称
     *
     * @param annotation {@link A}
     * @return
     */
    @Override
    protected String resolveMonitorConfigBeanName(Reference annotation) {
        return annotation.monitor();
    }

    @Override
    protected void postConfigureBean(Reference annotation, ReferenceBean bean) throws Exception {

        bean.setApplicationContext(applicationContext);
        // 注解 Reference 配置对应的接口 interfaceClass
        configureInterface(annotation, bean);

        // 配置消费者
        configureConsumerConfig(annotation, bean);

        bean.afterPropertiesSet();

    }

    public static ReferenceBeanBuilder create(Reference annotation, ClassLoader classLoader,
                                              ApplicationContext applicationContext) {
        return new ReferenceBeanBuilder(annotation, classLoader, applicationContext);
    }

}
