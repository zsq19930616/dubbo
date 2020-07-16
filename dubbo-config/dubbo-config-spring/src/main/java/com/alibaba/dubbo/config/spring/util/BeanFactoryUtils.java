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
package com.alibaba.dubbo.config.spring.util;

import com.alibaba.dubbo.common.utils.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.beans.factory.BeanFactoryUtils.beanNamesForTypeIncludingAncestors;
import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;

/**
 * {@link BeanFactory} Utilities class
 *
 * @see BeanFactory
 * @see ConfigurableBeanFactory
 * @see org.springframework.beans.factory.BeanFactoryUtils
 * @since 2.5.7
 */
public class BeanFactoryUtils {


    /**
     * Get optional Bean
     *
     * @param beanFactory {@link ListableBeanFactory}
     * @param beanName    the name of Bean
     * @param beanType    the {@link Class type} of Bean
     * @param <T>         the {@link Class type} of Bean
     * @return A bean if present , or <code>null</code>
     */
    public static <T> T getOptionalBean(ListableBeanFactory beanFactory, String beanName, Class<T> beanType) {

        // 获取所有的beanNames
        String[] allBeanNames = beanNamesForTypeIncludingAncestors(beanFactory, beanType);

        // 不包含，返回 null
        if (!StringUtils.isContains(allBeanNames, beanName)) {
            return null;
        }
        // 创建bean Map集合
        Map<String, T> beansOfType = beansOfTypeIncludingAncestors(beanFactory, beanType);
        // 获取对应的bean
        return beansOfType.get(beanName);

    }


    /**
     * Gets name-matched Beans from {@link ListableBeanFactory BeanFactory}
     *
     * @param beanFactory {@link ListableBeanFactory BeanFactory}
     * @param beanNames   the names of Bean
     * @param beanType    the {@link Class type} of Bean
     * @param <T>         the {@link Class type} of Bean
     * @return
     */
    public static <T> List<T> getBeans(ListableBeanFactory beanFactory, String[] beanNames, Class<T> beanType) {

        // bean的名称数组
        String[] allBeanNames = beanNamesForTypeIncludingAncestors(beanFactory, beanType);

        // 创建对应集合
        List<T> beans = new ArrayList<T>(beanNames.length);

        // 遍历
        for (String beanName : beanNames) {
            // 看你的beanName是不是在 allBeanNames 中
            if (StringUtils.isContains(allBeanNames, beanName)) {
                // 在了，添加到beans中
                // beanFactory.getBean(xx,xx) 根据名称和类型生成对应的bean
                beans.add(beanFactory.getBean(beanName, beanType));
            }
        }

        return Collections.unmodifiableList(beans);

    }

}
