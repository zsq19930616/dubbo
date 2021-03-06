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
import com.alibaba.dubbo.common.compiler.support.AdaptiveCompiler;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.support.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * ApplicationConfig
 * <p>
 * 应用配置
 * 属性参见 http://dubbo.io/books/dubbo-user-book/references/xml/dubbo-application.html
 *
 * @export
 */
public class ApplicationConfig extends AbstractConfig {

    private static final long serialVersionUID = 5508512956753757169L;

    // application name
    // 应用名称
    private String name;

    // module version
    // 版本号
    private String version;

    // application owner
    // 作者
    private String owner;

    // application's organization (BU)
    // 机构部门
    private String organization;

    // architecture layer
    // 架构层面
    private String architecture;

    // environment, e.g. dev, test or production
    // 服务环境
    private String environment;

    // Java compiler
    // 字节码编译器 一般为动态生成字节码
    private String compiler; // TODO 芋艿

    // logger
    // 日期支持
    private String logger;

    // registry centers
    // 注册中心配置集合
    private List<RegistryConfig> registries;

    // monitor center
    // 监控中心配置
    private MonitorConfig monitor;

    // is default or not
    // 是否默认
    private Boolean isDefault; // TODO 芋艿

    /**
     * directory for saving thread dump
     *
     * @see <a href="http://dubbo.io/books/dubbo-user-book/demos/dump.html">线程栈自动 dump</a>
     */
    // 转储目录
    private String dumpDirectory; // TODO 芋艿

    // qos 启用
    private Boolean qosEnable; // TODO 芋艿

    // qos 端口
    private Integer qosPort;

    // qos 异步
    private Boolean qosAcceptForeignIp;

    // customized parameters
    private Map<String, String> parameters;

    // 无参构造器
    public ApplicationConfig() {
        System.out.println("ApplicationConfig无参构造器");
    }

    // new 对象，初始化应用名称构造器
    public ApplicationConfig(String name) {
        // 设置应用名称
        setName(name);
    }

    @Parameter(key = Constants.APPLICATION_KEY, required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        // 检查名称
        checkName("name", name);
        this.name = name;
        // 如果id没有值，默认将 应用名称 赋值给 id
        if (id == null || id.length() == 0) {
            id = name;
        }
    }

    /**
     * 应用的版本号
     *
     * @return
     */
    @Parameter(key = "application.version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        checkMultiName("owner", owner);
        this.owner = owner;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        checkName("organization", organization);
        this.organization = organization;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        checkName("architecture", architecture);
        this.architecture = architecture;
    }

    public String getEnvironment() {
        return environment;
    }

    /**
     * 设置环境，仅支持 develop、test、product 这个几个环境.默认是 product
     *
     * @param environment
     */
    public void setEnvironment(String environment) {
        checkName("environment", environment);
        if (environment != null) {
            if (!("develop".equals(environment) || "test".equals(environment) || "product".equals(environment))) {
                throw new IllegalStateException("Unsupported environment: " + environment + ", only support develop/test/product, default is product.");
            }
        }
        this.environment = environment;
    }

    /**
     * 获取注册中心配置项
     *
     * @return
     */
    public RegistryConfig getRegistry() {
        return registries == null || registries.isEmpty() ? null : registries.get(0);
    }

    public void setRegistry(RegistryConfig registry) {
        List<RegistryConfig> registries = new ArrayList<RegistryConfig>(1);
        registries.add(registry);
        this.registries = registries;
    }

    public List<RegistryConfig> getRegistries() {
        return registries;
    }

    @SuppressWarnings({"unchecked"})
    public void setRegistries(List<? extends RegistryConfig> registries) {
        this.registries = (List<RegistryConfig>) registries;
    }

    /**
     * 获取监控中心配置
     *
     * @return
     */
    public MonitorConfig getMonitor() {
        return monitor;
    }

    public void setMonitor(MonitorConfig monitor) {
        this.monitor = monitor;
    }

    public void setMonitor(String monitor) {
        this.monitor = new MonitorConfig(monitor);
    }

    public String getCompiler() {
        return compiler;
    }

    public void setCompiler(String compiler) {
        this.compiler = compiler;
        AdaptiveCompiler.setDefaultCompiler(compiler);
    }

    public String getLogger() {
        return logger;
    }

    public void setLogger(String logger) {
        this.logger = logger;
        // 设置 LoggerAdapter
        LoggerFactory.setLoggerAdapter(logger);
    }

    public Boolean isDefault() {
        return isDefault;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Parameter(key = Constants.DUMP_DIRECTORY)
    public String getDumpDirectory() {
        return dumpDirectory;
    }

    public void setDumpDirectory(String dumpDirectory) {
        this.dumpDirectory = dumpDirectory;
    }

    @Parameter(key = Constants.QOS_ENABLE)
    public Boolean getQosEnable() {
        return qosEnable;
    }

    public void setQosEnable(Boolean qosEnable) {
        this.qosEnable = qosEnable;
    }

    @Parameter(key = Constants.QOS_PORT)
    public Integer getQosPort() {
        return qosPort;
    }

    public void setQosPort(Integer qosPort) {
        this.qosPort = qosPort;
    }

    @Parameter(key = Constants.ACCEPT_FOREIGN_IP)
    public Boolean getQosAcceptForeignIp() {
        return qosAcceptForeignIp;
    }

    public void setQosAcceptForeignIp(Boolean qosAcceptForeignIp) {
        this.qosAcceptForeignIp = qosAcceptForeignIp;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        checkParameterName(parameters);
        this.parameters = parameters;
    }
}