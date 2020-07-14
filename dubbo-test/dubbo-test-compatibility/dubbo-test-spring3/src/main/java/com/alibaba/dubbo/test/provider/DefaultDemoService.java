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
package com.alibaba.dubbo.test.provider;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.demo.DemoService;
import com.alibaba.dubbo.demo.ParamCallback;
import com.alibaba.dubbo.demo.entity.User;
import com.alibaba.dubbo.demo.enumx.Sex;

import java.util.Collection;

/**
 * Default {@link DemoService} implementation
 *
 * @since 2.5.8
 */
@Service(
        version = "2.5.8",
        application = "dubbo-annotation-provider",
        protocol = "dubbo",
        registry = "my-registry"
)
public class DefaultDemoService implements DemoService {

    @Override
    public String sayHello(String name) {
        return "DefaultDemoService - sayHell() : " + name;
    }

    @Override
    public void bye(Object o) {

    }

    @Override
    public void callbackParam(String msg, ParamCallback callback) {

    }

    @Override
    public String say01(String msg) {
        return null;
    }

    @Override
    public String[] say02() {
        return new String[0];
    }

    @Override
    public void say03() {

    }

    @Override
    public Void say04() {
        return null;
    }

    @Override
    public void save(User user) {

    }

    @Override
    public void update(User user) {

    }

    @Override
    public void delete(User user, Boolean vip) {

    }

    @Override
    public void saves(Collection<User> users) {

    }

    @Override
    public void saves(User[] users) {

    }

    @Override
    public void demo(String name, String password, User user) {

    }

    @Override
    public void demo(Sex sex) {

    }

    @Override
    public void hello(String name) {

    }

    @Override
    public void hello01(String name) {

    }

    @Override
    public void hello02(String name) {

    }

}
