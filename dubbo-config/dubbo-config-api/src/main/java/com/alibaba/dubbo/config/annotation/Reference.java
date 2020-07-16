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
package com.alibaba.dubbo.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Reference
 *
 * @export
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
// 引用注解类
public @interface Reference {

    // 接口类
    Class<?> interfaceClass() default void.class;

    // 接口名
    String interfaceName() default "";

    // 版本号
    String version() default "";

    // 组
    String group() default "";

    // 连接
    String url() default "";

    // 调用方
    String client() default "";

    // 是否泛化
    boolean generic() default false;

    // 是否在本机
    boolean injvm() default false;

    // 是否检查
    boolean check() default true;

    // 是否初始化
    boolean init() default false;

    // 是否懒加载
    boolean lazy() default false;

    // 是否存根
    boolean stubevent() default false;

    // 重连
    String reconnect() default "";

    boolean sticky() default false;

    String proxy() default "";

    String stub() default "";

    String cluster() default "";

    int connections() default 0;

    int callbacks() default 0;

    String onconnect() default "";

    String ondisconnect() default "";

    String owner() default "";

    String layer() default "";

    int retries() default 0;

    String loadbalance() default "";

    boolean async() default false;

    int actives() default 0;

    boolean sent() default false;

    String mock() default "";

    String validation() default "";

    int timeout() default 0;

    String cache() default "";

    String[] filter() default {};

    String[] listener() default {};

    String[] parameters() default {};

    String application() default "";

    String module() default "";

    String consumer() default "";

    String monitor() default "";

    String[] registry() default {};

}
