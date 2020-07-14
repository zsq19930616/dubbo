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
package com.alibaba.dubbo.config;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.config.support.Parameter;

import java.util.List;

/**
 * MethodConfig
 *
 * 方法级配置。
 * 属性参见：http://dubbo.io/books/dubbo-user-book/references/xml/dubbo-method.html 。
 *
 * @export
 */
public class MethodConfig extends AbstractMethodConfig {

    private static final long serialVersionUID = 884908855422675941L;

    // method name
    // 方法名
    private String name;

    // stat
    private Integer stat; // TODO 芋艿

    // whether to retry
    // 是否重试
    private Boolean retry;

    // if it's reliable
    // 是否可靠
    private Boolean reliable; // TODO 芋艿

    // thread limits for method invocations
    // 方法调用的线程限制
    private Integer executes; // TODO 芋艿

    // if it's deprecated
    // 服务方法是否过时，此属性只在<dubbo:method>作为<dubbo:service>子标签时有效
    private Boolean deprecated;

    // whether to enable sticky
    // 设置true 该接口上的所有方法使用同一个provider.如果需要更复杂的规则，请使用用路由
    private Boolean sticky; // TODO 芋艿

    // whether need to return
    // 是否需要返回值
    private Boolean isReturn; // TODO 芋艿

    // callback instance when async-call is invoked
    // 调用异步调用时的回调实例
    private Object oninvoke; // TODO 芋艿

    // callback method when async-call is invoked
    // 调用异步调用时的回调方法
    private String oninvokeMethod; // TODO 芋艿

    // callback instance when async-call is returned
    private Object onreturn; // TODO 芋艿

    // callback method when async-call is returned
    private String onreturnMethod; // TODO 芋艿

    // callback instance when async-call has exception thrown
    private Object onthrow; // TODO 芋艿

    // callback method when async-call has exception thrown
    private String onthrowMethod; // TODO 芋艿

    // 方法参数配置集合
    private List<ArgumentConfig> arguments;

    @Parameter(excluded = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        checkMethodName("name", name);
        this.name = name;
        if (id == null || id.length() == 0) {
            id = name;
        }
    }

    public Integer getStat() {
        return stat;
    }

    @Deprecated
    public void setStat(Integer stat) {
        this.stat = stat;
    }

    @Deprecated
    public Boolean isRetry() {
        return retry;
    }

    @Deprecated
    public void setRetry(Boolean retry) {
        this.retry = retry;
    }

    @Deprecated
    public Boolean isReliable() {
        return reliable;
    }

    @Deprecated
    public void setReliable(Boolean reliable) {
        this.reliable = reliable;
    }

    public Integer getExecutes() {
        return executes;
    }

    public void setExecutes(Integer executes) {
        this.executes = executes;
    }

    public Boolean getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public List<ArgumentConfig> getArguments() {
        return arguments;
    }

    @SuppressWarnings("unchecked")
    public void setArguments(List<? extends ArgumentConfig> arguments) {
        this.arguments = (List<ArgumentConfig>) arguments;
    }

    public Boolean getSticky() {
        return sticky;
    }

    public void setSticky(Boolean sticky) {
        this.sticky = sticky;
    }

    @Parameter(key = Constants.ON_RETURN_INSTANCE_KEY, excluded = true, attribute = true)
    public Object getOnreturn() {
        return onreturn;
    }

    public void setOnreturn(Object onreturn) {
        this.onreturn = onreturn;
    }

    @Parameter(key = Constants.ON_RETURN_METHOD_KEY, excluded = true, attribute = true)
    public String getOnreturnMethod() {
        return onreturnMethod;
    }

    public void setOnreturnMethod(String onreturnMethod) {
        this.onreturnMethod = onreturnMethod;
    }

    @Parameter(key = Constants.ON_THROW_INSTANCE_KEY, excluded = true, attribute = true)
    public Object getOnthrow() {
        return onthrow;
    }

    public void setOnthrow(Object onthrow) {
        this.onthrow = onthrow;
    }

    @Parameter(key = Constants.ON_THROW_METHOD_KEY, excluded = true, attribute = true)
    public String getOnthrowMethod() {
        return onthrowMethod;
    }

    public void setOnthrowMethod(String onthrowMethod) {
        this.onthrowMethod = onthrowMethod;
    }

    @Parameter(key = Constants.ON_INVOKE_INSTANCE_KEY, excluded = true, attribute = true)
    public Object getOninvoke() {
        return oninvoke;
    }

    public void setOninvoke(Object oninvoke) {
        this.oninvoke = oninvoke;
    }

    @Parameter(key = Constants.ON_INVOKE_METHOD_KEY, excluded = true, attribute = true)
    public String getOninvokeMethod() {
        return oninvokeMethod;
    }

    public void setOninvokeMethod(String oninvokeMethod) {
        this.oninvokeMethod = oninvokeMethod;
    }

    public Boolean isReturn() {
        return isReturn;
    }

    public void setReturn(Boolean isReturn) {
        this.isReturn = isReturn;
    }

}