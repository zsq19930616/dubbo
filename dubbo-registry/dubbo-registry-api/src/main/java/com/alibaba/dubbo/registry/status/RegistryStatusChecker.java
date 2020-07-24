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
package com.alibaba.dubbo.registry.status;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.status.Status;
import com.alibaba.dubbo.common.status.StatusChecker;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.support.AbstractRegistryFactory;

import java.util.Collection;

/**
 * RegistryStatusChecker
 * 注册中心状态检查
 */
@Activate
public class RegistryStatusChecker implements StatusChecker {

    public Status check() {
        // 获取注册中心集合
        Collection<Registry> regsitries = AbstractRegistryFactory.getRegistries();
        // 为空
        if (regsitries.isEmpty()) {
            // 返回 unknown
            return new Status(Status.Level.UNKNOWN);
        }
        // 默认是 OK 的
        Status.Level level = Status.Level.OK;
        // 字符串逗号拼接注册中心
        StringBuilder buf = new StringBuilder();
        for (Registry registry : regsitries) {
            // 第一次不用拼接 逗号
            if (buf.length() > 0) {
                buf.append(",");
            }
            // 拼接 address 后面链接状态
            buf.append(registry.getUrl().getAddress());
            // 注册中心不可用
            if (!registry.isAvailable()) {
                // 设为错误
                level = Status.Level.ERROR;
                buf.append("(disconnected)");
            } else {
                // 可用，正常连接
                buf.append("(connected)");
            }
        }
        return new Status(level, buf.toString());
    }

}