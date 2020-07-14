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

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.status.StatusChecker;
import com.alibaba.dubbo.common.threadpool.ThreadPool;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.config.support.Parameter;
import com.alibaba.dubbo.registry.support.AbstractRegistryFactory;
import com.alibaba.dubbo.remoting.Codec;
import com.alibaba.dubbo.remoting.Dispatcher;
import com.alibaba.dubbo.remoting.Transporter;
import com.alibaba.dubbo.remoting.exchange.Exchanger;
import com.alibaba.dubbo.remoting.telnet.TelnetHandler;
import com.alibaba.dubbo.rpc.Protocol;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ProtocolConfig
 *
 * 服务提供者协议配置
 * 属性参数 http://dubbo.io/books/dubbo-user-book/references/xml/dubbo-protocol.html
 *
 * @export
 */
public class ProtocolConfig extends AbstractConfig {

    private static final long serialVersionUID = 6913423882496634749L;

    // protocol name
    // 协议名称
    private String name;

    // service IP address (when there are multiple network cards available)
    // host 域名或者ip
    private String host;

    // service port
    // 端口
    private Integer port;

    // context path
    // 上下文路径
    private String contextpath;

    /**
     * 线程池名 {@link ThreadPool}
     */
    // thread pool
    private String threadpool;
    /**
     * 线程数 {@link com.alibaba.dubbo.common.threadpool.support.fixed.FixedThreadPool}
     */
    // thread pool size (fixed size)
    // 线程池大小
    private Integer threads;

    // IO thread pool size (fixed size)
    // io 线程数
    private Integer iothreads;
    /**
     * 队列数
     */
    // thread pool's queue length
    // 队列
    private Integer queues;

    // max acceptable connections
    // 最大连接数
    private Integer accepts;

    // protocol codec
    // 编解码器
    private String codec;

    // serialization
    // 序列化
    private String serialization;

    // charset
    // 字符集
    private String charset;

    // payload max length
    // 最大负载
    private Integer payload;

    // buffer size
    // 缓冲区大小
    private Integer buffer;

    // heartbeat interval
    // 心跳间隔
    private Integer heartbeat;

    // access log
    // 访问日志
    private String accesslog;

    // transfort
    // 传输类型
    private String transporter;

    // how information is exchanged
    // 如何交换信息
    private String exchanger;

    // thread dispatch mode
    // 线程调度模式
    private String dispatcher;

    // networker
    private String networker; // TODO ，芋艿

    // sever impl
    // 服务提供者
    private String server;

    // client impl
    // 客户端
    private String client;

    // supported telnet commands, separated with comma.
    // telnet命令
    private String telnet;

    // command line prompt
    // 命令行提示
    private String prompt; // TODO ，芋艿

    // status check
    // 状态检查
    private String status; // TODO ，芋艿

    // whether to register
    // 是否注册
    private Boolean register;

    // parameters
    // 是否长连接
    // TODO add this to provider config
    // 是否长连接
    private Boolean keepAlive;

    // TODO add this to provider config
    // 优化器
    private String optimizer;

    // 扩展
    private String extension;

    // parameters
    // 参数
    private Map<String, String> parameters;

    // if it's default
    // 是否默认
    private Boolean isDefault;
    /**
     * 是否已经销毁
     * 多线程，cas类保证原子性
     */
    private static final AtomicBoolean destroyed = new AtomicBoolean(false);

    public ProtocolConfig() {
    }

    // 初始化带协议名称
    public ProtocolConfig(String name) {
        setName(name);
    }

    // 协议及端口
    public ProtocolConfig(String name, int port) {
        setName(name);
        setPort(port);
    }

    // TODO: 2017/8/30 to move this method somewhere else
    public static void destroyAll() {
        // 忽略，若已经销毁
        // 预期值是 false，新值为 true，替换失败，已经销毁
        // destroyed 默认值是 false，替换 为 true
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }
        // 销毁 Registry 相关
        AbstractRegistryFactory.destroyAll();

        // 等到服务消费，接收到注册中心通知到该服务提供者已经下线，加大了在不重试情况下优雅停机的成功率。
        // Wait for registry notification
        try {
            Thread.sleep(ConfigUtils.getServerShutdownTimeout());
        } catch (InterruptedException e) {
            logger.warn("Interrupted unexpectedly when waiting for registry notification during shutdown process!");
        }

        // 销毁 Protocol 相关
        ExtensionLoader<Protocol> loader = ExtensionLoader.getExtensionLoader(Protocol.class);
        for (String protocolName : loader.getLoadedExtensions()) {
            try {
                Protocol protocol = loader.getLoadedExtension(protocolName);
                if (protocol != null) {
                    protocol.destroy();
                }
            } catch (Throwable t) {
                logger.warn(t.getMessage(), t);
            }
        }
    }

    @Parameter(excluded = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        checkName("name", name);
        this.name = name;
        if (id == null || id.length() == 0) {
            id = name;
        }
    }

    @Parameter(excluded = true)
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        checkName("host", host);
        this.host = host;
    }

    @Parameter(excluded = true)
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Deprecated
    @Parameter(excluded = true)
    public String getPath() {
        return getContextpath();
    }

    @Deprecated
    public void setPath(String path) {
        setContextpath(path);
    }

    @Parameter(excluded = true)
    public String getContextpath() {
        return contextpath;
    }

    public void setContextpath(String contextpath) {
        checkPathName("contextpath", contextpath);
        this.contextpath = contextpath;
    }

    public String getThreadpool() {
        return threadpool;
    }

    public void setThreadpool(String threadpool) {
        checkExtension(ThreadPool.class, "threadpool", threadpool);
        this.threadpool = threadpool;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public Integer getIothreads() {
        return iothreads;
    }

    public void setIothreads(Integer iothreads) {
        this.iothreads = iothreads;
    }

    public Integer getQueues() {
        return queues;
    }

    public void setQueues(Integer queues) {
        this.queues = queues;
    }

    public Integer getAccepts() {
        return accepts;
    }

    public void setAccepts(Integer accepts) {
        this.accepts = accepts;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        if ("dubbo".equals(name)) {
            checkMultiExtension(Codec.class, "codec", codec);
        }
        this.codec = codec;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        if ("dubbo".equals(name)) {
            checkMultiExtension(Serialization.class, "serialization", serialization);
        }
        this.serialization = serialization;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public Integer getPayload() {
        return payload;
    }

    public void setPayload(Integer payload) {
        this.payload = payload;
    }

    public Integer getBuffer() {
        return buffer;
    }

    public void setBuffer(Integer buffer) {
        this.buffer = buffer;
    }

    public Integer getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(Integer heartbeat) {
        this.heartbeat = heartbeat;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        if ("dubbo".equals(name)) {
            checkMultiExtension(Transporter.class, "server", server);
        }
        this.server = server;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        if ("dubbo".equals(name)) {
            checkMultiExtension(Transporter.class, "client", client);
        }
        this.client = client;
    }

    public String getAccesslog() {
        return accesslog;
    }

    public void setAccesslog(String accesslog) {
        this.accesslog = accesslog;
    }

    public String getTelnet() {
        return telnet;
    }

    public void setTelnet(String telnet) {
        checkMultiExtension(TelnetHandler.class, "telnet", telnet);
        this.telnet = telnet;
    }

    @Parameter(escaped = true)
    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        checkMultiExtension(StatusChecker.class, "status", status);
        this.status = status;
    }

    public Boolean isRegister() {
        return register;
    }

    public void setRegister(Boolean register) {
        this.register = register;
    }

    public String getTransporter() {
        return transporter;
    }

    public void setTransporter(String transporter) {
        checkExtension(Transporter.class, "transporter", transporter);
        this.transporter = transporter;
    }

    public String getExchanger() {
        return exchanger;
    }

    public void setExchanger(String exchanger) {
        checkExtension(Exchanger.class, "exchanger", exchanger);
        this.exchanger = exchanger;
    }

    /**
     * typo, switch to use {@link #getDispatcher()}
     *
     * @deprecated {@link #getDispatcher()}
     */
    @Deprecated
    @Parameter(excluded = true)
    public String getDispather() {
        return getDispatcher();
    }

    /**
     * typo, switch to use {@link #getDispatcher()}
     *
     * @deprecated {@link #setDispatcher(String)}
     */
    @Deprecated
    public void setDispather(String dispather) {
        setDispatcher(dispather);
    }

    public String getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(String dispatcher) {
        checkExtension(Dispatcher.class, "dispacther", dispatcher);
        this.dispatcher = dispatcher;
    }

    public String getNetworker() {
        return networker;
    }

    public void setNetworker(String networker) {
        this.networker = networker;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Boolean isDefault() {
        return isDefault;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public String getOptimizer() {
        return optimizer;
    }

    public void setOptimizer(String optimizer) {
        this.optimizer = optimizer;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void destory() {
        if (name != null) {
            ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(name).destroy();
        }
    }

}