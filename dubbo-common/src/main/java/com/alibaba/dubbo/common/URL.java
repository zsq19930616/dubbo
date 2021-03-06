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
package com.alibaba.dubbo.common;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * URL - Uniform Resource Locator (Immutable, ThreadSafe)
 * <p>
 * url example:
 * <ul>
 * <li>http://www.facebook.com/friends?param1=value1&amp;param2=value2
 * <li>http://username:password@10.20.130.230:8080/list?version=1.0.0
 * <li>ftp://username:password@192.168.1.7:21/1/read.txt
 * <li>registry://192.168.1.7:9090/com.alibaba.service1?param1=value1&amp;param2=value2
 * </ul>
 * <p>
 * Some strange example below:
 * <ul>
 * <li>192.168.1.3:20880<br>
 * for this case, url protocol = null, url host = 192.168.1.3, port = 20880, url path = null
 * <li>file:///home/user1/router.js?type=script<br>
 * for this case, url protocol = null, url host = null, url path = home/user1/router.js
 * <li>file://home/user1/router.js?type=script<br>
 * for this case, url protocol = file, url host = home, url path = user1/router.js
 * <li>file:///D:/1/router.js?type=script<br>
 * for this case, url protocol = file, url host = null, url path = D:/1/router.js
 * <li>file:/D:/1/router.js?type=script<br>
 * same as above file:///D:/1/router.js?type=script
 * <li>/home/user1/router.js?type=script <br>
 * for this case, url protocol = null, url host = null, url path = home/user1/router.js
 * <li>home/user1/router.js?type=script <br>
 * for this case, url protocol = null, url host = home, url path = user1/router.js
 * </ul>
 *
 * 格式：protocol://username:password@host:port/path?key=value&key=value
 * 所有配置最终都将转换为 URL 表示，并由服务提供方生成，经注册中心传递给消费方，各属性对应 URL 的参数，参见配置项一览表中的 "对应URL参数" 列。
 * 来自 <a href="https://dubbo.gitbooks.io/dubbo-user-book/references/xml/introduction.html">schema 配置参考手册</>
 * 格式：protocol://username:password@host:port/path?key=value&key=value
 * 示例：dubbo://192.168.3.17:20880/com.alibaba.dubbo.demo.DemoService?anyhost=true&application=demo-provider&default.delay=-1&default.retries=0&default.service.filter=demoFilter&delay=-1&dubbo=2.0.0&generic=false&interface=com.alibaba.dubbo.demo.DemoService&methods=sayHello&pid=19031&side=provider&timestamp=1519651641799
 * @see java.net.URL
 * @see java.net.URI
 */
public final class URL implements Serializable {

    private static final long serialVersionUID = -1985165475234910535L;

    /**
     * 协议名
     */
    private final String protocol;
    /**
     * 用户名
     */
    private final String username;
    /**
     * 密码
     */
    private final String password;

    /**
     * by default, host to registry
     * 地址
     */
    private final String host;
    /**
     * by default, port to registry
     * 端口
     */
    private final int port;
    /**
     * 路径（服务名）
     */
    private final String path;
    /**
     * 参数集合
     */
    private final Map<String, String> parameters;

    // ==== cache ====

    private volatile transient Map<String, Number> numbers;

    private volatile transient Map<String, URL> urls;

    private volatile transient String ip;

    private volatile transient String full;

    private volatile transient String identity;

    private volatile transient String parameter;

    private volatile transient String string;

    protected URL() {
        this.protocol = null;
        this.username = null;
        this.password = null;
        this.host = null;
        this.port = 0;
        this.path = null;
        this.parameters = null;
    }

    public URL(String protocol, String host, int port) {
        this(protocol, null, null, host, port, null, (Map<String, String>) null);
    }

    public URL(String protocol, String host, int port, String[] pairs) { // varargs ... confilict with the following path argument, use array instead.
        this(protocol, null, null, host, port, null, CollectionUtils.toStringMap(pairs));
    }

    public URL(String protocol, String host, int port, Map<String, String> parameters) {
        this(protocol, null, null, host, port, null, parameters);
    }

    public URL(String protocol, String host, int port, String path) {
        this(protocol, null, null, host, port, path, (Map<String, String>) null);
    }

    public URL(String protocol, String host, int port, String path, String... pairs) {
        this(protocol, null, null, host, port, path, CollectionUtils.toStringMap(pairs));
    }

    public URL(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this(protocol, null, null, host, port, path, parameters);
    }

    public URL(String protocol, String username, String password, String host, int port, String path) {
        this(protocol, username, password, host, port, path, (Map<String, String>) null);
    }

    public URL(String protocol, String username, String password, String host, int port, String path, String... pairs) {
        this(protocol, username, password, host, port, path, CollectionUtils.toStringMap(pairs));
    }

    public URL(String protocol, String username, String password, String host, int port, String path, Map<String, String> parameters) {
        if ((username == null || username.length() == 0)
                && password != null && password.length() > 0) {
            throw new IllegalArgumentException("Invalid url, password without username!");
        }
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = (port < 0 ? 0 : port);
        // trim the beginning "/"
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        this.path = path;
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        } else {
            parameters = new HashMap<String, String>(parameters);
        }
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    /**
     * Parse url string
     * 解析字符串 url为 URL
     * @param url URL string
     * @return URL instance
     * @see URL
     */
    public static URL valueOf(String url) {
        if (url == null || (url = url.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }
        // 协议
        String protocol = null;
        // 用户名
        String username = null;
        // 密码
        String password = null;
        // 地址
        String host = null;
        // 端口
        int port = 0;
        // 路径
        String path = null;
        Map<String, String> parameters = null;
        // ? 参数解析开始
        // 获取 ?位置
        int i = url.indexOf("?"); // seperator between body and parameters
        // 有参数
        if (i >= 0) {
            // 参数数组
            String[] parts = url.substring(i + 1).split("\\&");
            // 转为 map
            parameters = new HashMap<String, String>();
            for (String part : parts) {
                // key = value
                part = part.trim();
                // key 长度大于0
                if (part.length() > 0) {
                    // 获取 = 的位置
                    int j = part.indexOf('=');
                    // 有等号
                    if (j >= 0) {
                        // 拆分放入map中
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        // 没有，设置为一样的值，放入map中
                        parameters.put(part, part);
                    }
                }
            }
            // ? 参数处理完毕，设置url
            url = url.substring(0, i);
        }
        // ? 参数解析结束
        // 获取协议名称
        // 协议解析开始
        i = url.indexOf("://");
        // 不可为空！
        if (i >= 0) {
            // 没有协议，抛出异常.意思就是 :// 不能在字符串的最开头。
            if (i == 0) throw new IllegalStateException("url missing protocol: \"" + url + "\"");
            // 设置协议
            protocol = url.substring(0, i);
            // 设置url  "://" 的长度
            url = url.substring(i + 3);
        } else {
            // case: file:/path/to/file.txt
            i = url.indexOf(":/");
            if (i >= 0) {
                // 同理 :/ 不能在字符串的开始位置，第一个位置代表没有协议喽
                if (i == 0) throw new IllegalStateException("url missing protocol: \"" + url + "\"");
                // 获取协议名称
                protocol = url.substring(0, i);
                // 设置url
                url = url.substring(i + 1);
            }
        }
        // 协议解析完毕
        // 解析path开始
        // 服务名称。一般一个应用就是一个服务，dubbo，接口就是服务。
        i = url.indexOf("/");
        if (i >= 0) {
            path = url.substring(i + 1);
            // 设置url。 **/path?
            // url = **
            url = url.substring(0, i);
        }
        // 解析path结束
        // username:password@host:port
        // 解析用户名密码
        i = url.indexOf("@");
        if (i >= 0) {
            // 获取用户名
            username = url.substring(0, i);
            // 看看有么有 : 咯
            int j = username.indexOf(":");
            // 有
            if (j >= 0) {
                // 设置密码
                password = username.substring(j + 1);
                // 设置用户名
                username = username.substring(0, j);
            }
            // 设置剩下的玩意
            // host:port
            url = url.substring(i + 1);
        }
        // 解析用户名密码完毕
        // 解析地址和端口开始
        // 判断是否有 :
        i = url.indexOf(":");
        // 有
        if (i >= 0 && i < url.length() - 1) {
            // 端口肯定是 int 嘞
            port = Integer.parseInt(url.substring(i + 1));
            // 设置url
            // url = address
            url = url.substring(0, i);
        }
        // 解析地址和端口结束
        if (url.length() > 0) host = url;
        // 生成 URL 对象 /(ㄒoㄒ)/~~
        return new URL(protocol, username, password, host, port, path, parameters);
    }

    /**
     * 编码
     * @param value
     * @return
     */
    public static String encode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 解码
     * @param value
     * @return
     */
    public static String decode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // 获取协议
    public String getProtocol() {
        return protocol;
    }

    // 设置协议，生成新的 URL 对象
    public URL setProtocol(String protocol) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    // 获取用户名
    public String getUsername() {
        return username;
    }

    // 设置用户名 生成新的 URL 对象
    public URL setUsername(String username) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    // 获取密码
    public String getPassword() {
        return password;
    }

    // 设置密码，生成新的 URL 对象
    public URL setPassword(String password) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    // 获取授权信息
    public String getAuthority() {
        // 没有用户名密码，返回 null
        if ((username == null || username.length() == 0)
                && (password == null || password.length() == 0)) {
            return null;
        }
        // 返回 username:password
        return (username == null ? "" : username)
                + ":" + (password == null ? "" : password);
    }

    // 获取 主机
    public String getHost() {
        return host;
    }

    // 设置 主机地址，返回 新的 URL 对象
    public URL setHost(String host) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    /**
     * Fetch IP address for this URL.
     *
     * Pls. note that IP should be used instead of Host when to compare with socket's address or to search in a map
     * which use address as its key.
     *
     * @return ip in string format
     */
    // 获取 ip 地址
    public String getIp() {
        if (ip == null) {
            ip = NetUtils.getIpByHost(host);
        }
        return ip;
    }

    // 获取端口
    public int getPort() {
        return port;
    }

    // 设置端口，生成新的 URL 对象
    public URL setPort(int port) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    // 获取端口，没有指定返回默认端口
    public int getPort(int defaultPort) {
        return port <= 0 ? defaultPort : port;
    }

    // 获取 address ===>  host:port
    public String getAddress() {
        return port <= 0 ? host : host + ":" + port;
    }

    // 设置地址信息
    public URL setAddress(String address) {
        // 获取 :
        int i = address.lastIndexOf(':');
        // 主机
        String host;
        // 端口默认是 当前对象的端口
        int port = this.port;
        if (i >= 0) {
            // 冒号之前的是 主机
            host = address.substring(0, i);
            // 冒号之后的是 端口
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            // 没有冒号，都是 主机
            host = address;
        }
        // 生成 URL 对象
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    // 备份地址
    public String getBackupAddress() {
        return getBackupAddress(0);
    }

    // 获取指定端口的备份地址，咋来的呢？
    public String getBackupAddress(int defaultPort) {
        // getAddress() ===> host:port
        // appendDefaultPort() ===> 返回port不为0的address  host:port
        StringBuilder address = new StringBuilder(appendDefaultPort(getAddress(), defaultPort));
        // 获取 parameter  // key:backup
        // 设置的备份地址！！！  有备胎，可以的。
        String[] backups = getParameter(Constants.BACKUP_KEY, new String[0]);
        // 拿到了值
        if (backups != null && backups.length > 0) {
            // 遍历
            for (String backup : backups) {
                // 拼接到 address 后面。
                address.append(",");
                address.append(appendDefaultPort(backup, defaultPort));
            }
        }
        return address.toString();
    }

    // 多个 备胎 url 集合
    public List<URL> getBackupUrls() {
        List<URL> urls = new ArrayList<URL>();
        // 添加当前的url
        urls.add(this);
        // 获取备份地址
        String[] backups = getParameter(Constants.BACKUP_KEY, new String[0]);
        if (backups != null && backups.length > 0) {
            // 备胎多就是好啊。可选择性多，随时替换下一个！！！
            for (String backup : backups) {
                urls.add(this.setAddress(backup));
            }
        }
        return urls;
    }

    private String appendDefaultPort(String address, int defaultPort) {
        // address 不为空，默认端口大于0
        if (address != null && address.length() > 0
                && defaultPort > 0) {
            // 检测 address 是否有 :
            int i = address.indexOf(':');
            // 没有
            if (i < 0) {
                // 那就拼接返回了
                return address + ":" + defaultPort;
            } else if (Integer.parseInt(address.substring(i + 1)) == 0) {
                // 截取端口为 0，设置 host:defaultPort
                return address.substring(0, i + 1) + defaultPort;
            }
        }
        // 最终返回一个 host:端口不为0的通信地址
        return address;
    }

    // 获取 应用
    public String getPath() {
        return path;
    }

    // 设置应用
    public URL setPath(String path) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    // 获取绝对接口路径，其实就是添加一个 / 开头而已
    public String getAbsolutePath() {
        if (path != null && !path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }

    // 参数集合
    public Map<String, String> getParameters() {
        return parameters;
    }

    // 指定参数解码
    public String getParameterAndDecoded(String key) {
        return getParameterAndDecoded(key, null);
    }

    // 指定参数解码，没有值给个默认值
    public String getParameterAndDecoded(String key, String defaultValue) {
        return decode(getParameter(key, defaultValue));
    }

    public String getParameter(String key) {
        // 获取value
        String value = parameters.get(key);
        // 为空，坚持一下，加个前缀获取一下了
        if (value == null || value.length() == 0) {
            // 再次获取 default.backup  脸皮真厚。
            value = parameters.get(Constants.DEFAULT_KEY_PREFIX + key);
        }
        return value;
    }

    // 获取参数，没有值给个默认值
    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public String[] getParameter(String key, String[] defaultValue) {
        String value = getParameter(key);
        // 没有获取到，返回默认值
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        // 正则拆分。  逗号分割的
        return Constants.COMMA_SPLIT_PATTERN.split(value);
    }

    // 线程可见的属性，用的时候在初始化
    private Map<String, Number> getNumbers() {
        if (numbers == null) { // concurrent initialization is tolerant
            numbers = new ConcurrentHashMap<String, Number>();
        }
        return numbers;
    }

    // 所有的url，用的时候初始化
    private Map<String, URL> getUrls() {
        if (urls == null) { // concurrent initialization is tolerant
            urls = new ConcurrentHashMap<String, URL>();
        }
        return urls;
    }

    // 通过 key 获取指定的 URL
    public URL getUrlParameter(String key) {
        // map 中获取
        URL u = getUrls().get(key);
        // 找到了
        if (u != null) {
            // 返回
            return u;
        }
        // 没找到，那就解码，再找一下
        String value = getParameterAndDecoded(key);
        // 还没有
        if (value == null || value.length() == 0) {
            // 返回 null
            return null;
        }
        // 有，那就编码，能看懂返回去。
        u = URL.valueOf(value);
        // 把能看懂的添加到集合中，那么下次获取的时候就不用编码了。优秀！
        getUrls().put(key, u);
        return u;
    }

    // 获取 值为 double 的
    public double getParameter(String key, double defaultValue) {
        // Number 是 父类
        Number n = getNumbers().get(key);
        // 找到了
        if (n != null) {
            // 转为 double 返回
            return n.doubleValue();
        }
        // 没有，厚脸皮再来一次， 加个前缀搞一把， 再次获取 ，说不定有 default 配置
        String value = getParameter(key);
        // 还没有哦
        if (value == null || value.length() == 0) {
            // 返回默认值
            return defaultValue;
        }
        // 找到了就转换成 double。
        double d = Double.parseDouble(value);
        // 添加到 集合中。下一次，就不用找了。
        getNumbers().put(key, d);
        // 返回
        return d;
    }

    // 骚操作都一样。重载的方法
    public float getParameter(String key, float defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.floatValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        float f = Float.parseFloat(value);
        getNumbers().put(key, f);
        return f;
    }

    // 骚操作都一样。重载的方法
    public long getParameter(String key, long defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.longValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        long l = Long.parseLong(value);
        getNumbers().put(key, l);
        return l;
    }

    // 骚操作都一样。重载的方法
    public int getParameter(String key, int defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.intValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        getNumbers().put(key, i);
        return i;
    }

    // 骚操作都一样。重载的方法
    public short getParameter(String key, short defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.shortValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        short s = Short.parseShort(value);
        getNumbers().put(key, s);
        return s;
    }

    // 骚操作都一样。重载的方法
    public byte getParameter(String key, byte defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.byteValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        byte b = Byte.parseByte(value);
        getNumbers().put(key, b);
        return b;
    }

    // 提前出错，积极一些。
    public float getPositiveParameter(String key, float defaultValue) {
        // 默认值不合法
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        // 获取
        float value = getParameter(key, defaultValue);
        // 小于0
        if (value <= 0) {
            // 返回默认值
            return defaultValue;
        }
        return value;
    }

    // 重载方法：提前出错，积极一些。
    public double getPositiveParameter(String key, double defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        double value = getParameter(key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    // 重载方法：提前出错，积极一些。
    public long getPositiveParameter(String key, long defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        long value = getParameter(key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    // 重载方法：提前出错，积极一些。
    public int getPositiveParameter(String key, int defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        int value = getParameter(key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    // 重载方法：提前出错，积极一些。
    public short getPositiveParameter(String key, short defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        short value = getParameter(key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    // 重载方法：提前出错，积极一些。
    public byte getPositiveParameter(String key, byte defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        byte value = getParameter(key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    // 重载方法：提前出错，积极一些。
    public char getParameter(String key, char defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value.charAt(0);
    }

    // 重载方法：提前出错，积极一些。
    public boolean getParameter(String key, boolean defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    // 判断是否有这个key
    public boolean hasParameter(String key) {
        // 获取值
        String value = getParameter(key);
        // 值不为空代表有
        return value != null && value.length() > 0;
    }

    // 方法+key
    // 获取方法参数并且解码
    public String getMethodParameterAndDecoded(String method, String key) {
        return URL.decode(getMethodParameter(method, key));
    }

    // 获取方法参数，没有拿到给个默认值，并且解码
    public String getMethodParameterAndDecoded(String method, String key, String defaultValue) {
        return URL.decode(getMethodParameter(method, key, defaultValue));
    }

    // 真实的方法来了，获取方法参数
    public String getMethodParameter(String method, String key) {
        // key 是 method.key
        // 获取值
        String value = parameters.get(method + "." + key);
        // 没有
        if (value == null || value.length() == 0) {
            // 再次获取，看下有没有 default 设置的值
            return getParameter(key);
        }
        return value;
    }

    // 重载方法，找不到就给个默认值
    public String getMethodParameter(String method, String key, String defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    // 方法参数也这样，就这个干就完了。
    // 有就返回，
    // 没有再次获取，还没有返回默认值
    // 解析成对应的类型，缓存起来，下次就直接找到了
    public double getMethodParameter(String method, String key, double defaultValue) {
        String methodKey = method + "." + key;
        Number n = getNumbers().get(methodKey);
        if (n != null) {
            return n.intValue();
        }
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        double d = Double.parseDouble(value);
        getNumbers().put(methodKey, d);
        return d;
    }

    public float getMethodParameter(String method, String key, float defaultValue) {
        String methodKey = method + "." + key;
        Number n = getNumbers().get(methodKey);
        if (n != null) {
            return n.intValue();
        }
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        float f = Float.parseFloat(value);
        getNumbers().put(methodKey, f);
        return f;
    }

    public long getMethodParameter(String method, String key, long defaultValue) {
        String methodKey = method + "." + key;
        Number n = getNumbers().get(methodKey);
        if (n != null) {
            return n.intValue();
        }
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        long l = Long.parseLong(value);
        getNumbers().put(methodKey, l);
        return l;
    }

    public int getMethodParameter(String method, String key, int defaultValue) {
        String methodKey = method + "." + key;
        Number n = getNumbers().get(methodKey);
        if (n != null) {
            return n.intValue();
        }
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        getNumbers().put(methodKey, i);
        return i;
    }

    public short getMethodParameter(String method, String key, short defaultValue) {
        String methodKey = method + "." + key;
        Number n = getNumbers().get(methodKey);
        if (n != null) {
            return n.shortValue();
        }
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        short s = Short.parseShort(value);
        getNumbers().put(methodKey, s);
        return s;
    }

    public byte getMethodParameter(String method, String key, byte defaultValue) {
        String methodKey = method + "." + key;
        Number n = getNumbers().get(methodKey);
        if (n != null) {
            return n.byteValue();
        }
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        byte b = Byte.parseByte(value);
        getNumbers().put(methodKey, b);
        return b;
    }

    // 默认值合法检测的
    public double getMethodPositiveParameter(String method, String key, double defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        double value = getMethodParameter(method, key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public float getMethodPositiveParameter(String method, String key, float defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        float value = getMethodParameter(method, key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public long getMethodPositiveParameter(String method, String key, long defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        long value = getMethodParameter(method, key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public int getMethodPositiveParameter(String method, String key, int defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        int value = getMethodParameter(method, key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public short getMethodPositiveParameter(String method, String key, short defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        short value = getMethodParameter(method, key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public byte getMethodPositiveParameter(String method, String key, byte defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        byte value = getMethodParameter(method, key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public char getMethodParameter(String method, String key, char defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value.charAt(0);
    }

    public boolean getMethodParameter(String method, String key, boolean defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    // 判断方法是否有这个参数
    public boolean hasMethodParameter(String method, String key) {
        // 方法名为空
        if (method == null) {
            // 后缀弄好
            String suffix = "." + key;
            // 遍历集合，有这个后缀的就代表有。！！！
            for (String fullKey : parameters.keySet()) {
                if (fullKey.endsWith(suffix)) {
                    return true;
                }
            }
            return false;
        }
        // key 为空
        if (key == null) {
            // 前缀拼好
            String prefix = method + ".";
            // 有这个开头的就代表有！！！
            for (String fullKey : parameters.keySet()) {
                if (fullKey.startsWith(prefix)) {
                    return true;
                }
            }
            return false;
        }
        // 没有特殊情况，正常取值
        String value = getMethodParameter(method, key);
        // 验证值是否为空
        return value != null && value.length() > 0;
    }

    /**
     * 判断是否是 本地host
     * @return
     */
    public boolean isLocalHost() {
        return NetUtils.isLocalHost(host) || getParameter(Constants.LOCALHOST_KEY, false);
    }

    /**
     * 是否任意host.在我理解就是任意IP都可以访问吧，暂定
     * @return
     */
    public boolean isAnyHost() {
        return Constants.ANYHOST_VALUE.equals(host) || getParameter(Constants.ANYHOST_KEY, false);
    }

    // 参数编码，放入map集合中
    public URL addParameterAndEncoded(String key, String value) {
        if (value == null || value.length() == 0) {
            return this;
        }
        return addParameter(key, encode(value));
    }

    // 又一波重载方法
    public URL addParameter(String key, boolean value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, char value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, byte value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, short value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, int value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, long value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, float value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, double value) {
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, Enum<?> value) {
        if (value == null) return this;
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, Number value) {
        if (value == null) return this;
        return addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, CharSequence value) {
        if (value == null || value.length() == 0) return this;
        return addParameter(key, String.valueOf(value));
    }

    // 真正添加参数的方法逻辑
    public URL addParameter(String key, String value) {
        // key 或者 value 不存在，直接返回当前对象
        if (key == null || key.length() == 0
                || value == null || value.length() == 0) {
            return this;
        }
        // if value doesn't change, return immediately
        // value已经存在了，直接返回
        if (value.equals(getParameters().get(key))) { // value != null
            return this;
        }
        // 创建新的集合对象
        Map<String, String> map = new HashMap<String, String>(getParameters());
        // 添加到集合中
        map.put(key, value);
        // 生成新的对象返回
        return new URL(protocol, username, password, host, port, path, map);
    }

    // 如果 key 不存在 添加 value，否则直接返回 this
    public URL addParameterIfAbsent(String key, String value) {
        if (key == null || key.length() == 0
                || value == null || value.length() == 0) {
            return this;
        }
        if (hasParameter(key)) {
            return this;
        }
        // 新增的值
        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.put(key, value);
        return new URL(protocol, username, password, host, port, path, map);
    }

    /**
     * Add parameters to a new url.
     *
     * @param parameters parameters in key-value pairs
     * @return A new URL
     */
    // 添加参数集合到新的 URL 对象中
    public URL addParameters(Map<String, String> parameters) {
        // 为空直接返回
        if (parameters == null || parameters.size() == 0) {
            return this;
        }

        boolean hasAndEqual = true;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            // this.getParameters()
            // 判断调用者中有没有这个key
            String value = getParameters().get(entry.getKey());
            // value 为空
            if (value == null) {
                // 我有
                if (entry.getValue() != null) {
                    // 不一样
                    hasAndEqual = false;
                    break;
                }
            } else {
                // 有，但是两个值不一样
                if (!value.equals(entry.getValue())) {
                    hasAndEqual = false;
                    break;
                }
            }
        }
        // return immediately if there's no change
        // 值都一样，就不用覆盖了，直接返回 this
        if (hasAndEqual) return this;

        // 有不一样的，方法参数中的map覆盖掉当前this中的。
        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.putAll(parameters);
        // 生成新的 URL 返回
        return new URL(protocol, username, password, host, port, path, map);
    }

    public URL addParametersIfAbsent(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(parameters);
        map.putAll(getParameters());
        return new URL(protocol, username, password, host, port, path, map);
    }

    // 来了个可变参数。。。
    // 看方法意思是  key,value,key,value... 这样的组合咯
    public URL addParameters(String... pairs) {
        // 为空，忽略
        if (pairs == null || pairs.length == 0) {
            return this;
        }
        // 不满足要求，抛出异常. 参数必须是偶数倍
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Map pairs can not be odd number.");
        }
        // 拆解到 map 中。
        Map<String, String> map = new HashMap<String, String>();
        int len = pairs.length / 2;
        for (int i = 0; i < len; i++) {
            map.put(pairs[2 * i], pairs[2 * i + 1]);
        }
        return addParameters(map);
    }

    // 添加 query 也就是 get 请求 ? 后面的一大坨子
    public URL addParameterString(String query) {
        // 没有忽略
        if (query == null || query.length() == 0) {
            return this;
        }
        // parseQueryString(String s) 解析成map，添加过去
        return addParameters(StringUtils.parseQueryString(query));
    }

    // 移除指定的参数
    public URL removeParameter(String key) {
        if (key == null || key.length() == 0) {
            return this;
        }
        return removeParameters(key);
    }

    public URL removeParameters(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return this;
        }
        return removeParameters(keys.toArray(new String[0]));
    }

    // 移除指定的后又生成新的 URL 对象
    public URL removeParameters(String... keys) {
        if (keys == null || keys.length == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        for (String key : keys) {
            map.remove(key);
        }
        if (map.size() == getParameters().size()) {
            return this;
        }
        return new URL(protocol, username, password, host, port, path, map);
    }

    // 返回没有参数的 URL
    public URL clearParameters() {
        return new URL(protocol, username, password, host, port, path, new HashMap<String, String>());
    }

    // 获取参数
    public String getRawParameter(String key) {
        if ("protocol".equals(key))
            return protocol;
        if ("username".equals(key))
            return username;
        if ("password".equals(key))
            return password;
        if ("host".equals(key))
            return host;
        if ("port".equals(key))
            return String.valueOf(port);
        if ("path".equals(key))
            return path;
        return getParameter(key);
    }

    // 转为 map返回
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>(parameters);
        if (protocol != null)
            map.put("protocol", protocol);
        if (username != null)
            map.put("username", username);
        if (password != null)
            map.put("password", password);
        if (host != null)
            map.put("host", host);
        if (port > 0)
            map.put("port", String.valueOf(port));
        if (path != null)
            map.put("path", path);
        return map;
    }

    public String toString() {
        // 有值就返回
        if (string != null) {
            return string;
        }
        // 构建返回
        return string = buildString(false, true); // no show username and password
    }

    public String toString(String... parameters) {
        return buildString(false, true, parameters); // no show username and password
    }

    public String toIdentityString() {
        if (identity != null) {
            return identity;
        }
        return identity = buildString(true, false); // only return identity message, see the method "equals" and "hashCode"
    }

    public String toIdentityString(String... parameters) {
        return buildString(true, false, parameters); // only return identity message, see the method "equals" and "hashCode"
    }

    public String toFullString() {
        if (full != null) {
            return full;
        }
        return full = buildString(true, true);
    }

    public String toFullString(String... parameters) {
        return buildString(true, true, parameters);
    }

    public String toParameterString() {
        if (parameter != null) {
            return parameter;
        }
        return parameter = toParameterString(new String[0]);
    }

    public String toParameterString(String... parameters) {
        StringBuilder buf = new StringBuilder();
        buildParameters(buf, false, parameters);
        return buf.toString();
    }

    private void buildParameters(StringBuilder buf, boolean concat, String[] parameters) {
        // 看看参数集合有么有值
        if (getParameters() != null && getParameters().size() > 0) {
            // 方法传入的参数.下面看了看啊，parameters为空，全部显示，不为空，显示指定的。奶奶个腿
            List<String> includes = (parameters == null || parameters.length == 0 ? null : Arrays.asList(parameters));
            // 是否第一次
            boolean first = true;
            for (Map.Entry<String, String> entry : new TreeMap<String, String>(getParameters()).entrySet()) {
                if (entry.getKey() != null && entry.getKey().length() > 0
                        && (includes == null || includes.contains(entry.getKey()))) {
                    // 第一次拼接个 ?
                    if (first) {
                        if (concat) {
                            buf.append("?");
                        }
                        first = false;
                    } else {
                        // 不是第一次了，那就拼接 &
                        buf.append("&");
                    }
                    // ?key=value&key=value
                    buf.append(entry.getKey());
                    buf.append("=");
                    buf.append(entry.getValue() == null ? "" : entry.getValue().trim());
                }
            }
        }
    }

    /**
     *
     * @param appendUser        是否展示用户
     * @param appendParameter   是否展示参数
     * @param parameters        参数
     * @return
     */
    private String buildString(boolean appendUser, boolean appendParameter, String... parameters) {
        return buildString(appendUser, appendParameter, false, false, parameters);
    }

    /**
     *
     * @param appendUser        是否显示用户
     * @param appendParameter   是否显示参数
     * @param useIP             是否显示IP
     * @param useService        是否显示service
     * @param parameters        参数
     * @return
     */
    private String buildString(boolean appendUser, boolean appendParameter, boolean useIP, boolean useService, String... parameters) {
        // 开始sb拼接
        StringBuilder buf = new StringBuilder();
        // 协议
        if (protocol != null && protocol.length() > 0) {
            buf.append(protocol);
            buf.append("://");
        }
        // 显示用户才拼接用户和密码
        if (appendUser && username != null && username.length() > 0) {
            buf.append(username);
            if (password != null && password.length() > 0) {
                buf.append(":");
                buf.append(password);
            }
            buf.append("@");
        }
        // 显示IP拼接IP，否则拼接 host 地址
        String host;
        if (useIP) {
            host = getIp();
        } else {
            host = getHost();
        }
        // 有端口就拼接
        if (host != null && host.length() > 0) {
            buf.append(host);
            if (port > 0) {
                buf.append(":");
                buf.append(port);
            }
        }
        // 应用名
        // getServiceKey 啥玩意？ 看一番.返回 接口/组:版本号
        String path;
        if (useService) {
            path = getServiceKey();
        } else {
            path = getPath();
        }
        // 来个前缀  /
        if (path != null && path.length() > 0) {
            buf.append("/");
            buf.append(path);
        }
        // 拼接参数
        if (appendParameter) {
            buildParameters(buf, true, parameters);
        }
        return buf.toString();
    }

    // 转为 JDK URL
    public java.net.URL toJavaURL() {
        try {
            return new java.net.URL(toString());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    // 转为 InetSocketAddress
    public InetSocketAddress toInetSocketAddress() {
        return new InetSocketAddress(host, port);
    }

    /**
     * 获得 Service 键
     *
     * 格式为 格式为 ${group}/${interface}:${version}
     * 和 {@link StringUtils#getServiceKey(Map)} 一致
     *
     * @return 键
     */
    public String getServiceKey() {
        // 接口名
        String inf = getServiceInterface();
        // 没找到，返回null
        if (inf == null) return null;
        // 找到了。
        StringBuilder buf = new StringBuilder();
        // 看看属于哪个组
        String group = getParameter(Constants.GROUP_KEY);
        // 有值就拼接
        if (group != null && group.length() > 0) {
            buf.append(group).append("/");
        }
        buf.append(inf);
        // 版本号也来看看
        String version = getParameter(Constants.VERSION_KEY);
        if (version != null && version.length() > 0) {
            buf.append(":").append(version);
        }
        return buf.toString();
    }

    // 只显示前面的信息
    // host 是 域名地址
    // 协议://username:password@host:port/path
    public String toServiceStringWithoutResolving() {
        return buildString(true, false, false, true);
    }

    // host 是 IP
    // 协议://username:password@host:port/path
    public String toServiceString() {
        return buildString(true, false, true, true);
    }

    @Deprecated
    public String getServiceName() {
        return getServiceInterface();
    }

    // 获取接口名
    public String getServiceInterface() {
        return getParameter(Constants.INTERFACE_KEY, path);
    }

    // 设置当前 URL 的接口名
    public URL setServiceInterface(String service) {
        return addParameter(Constants.INTERFACE_KEY, service);
    }

    /**
     * @see #getParameter(String, int)
     * @deprecated Replace to <code>getParameter(String, int)</code>
     */
    @Deprecated
    public int getIntParameter(String key) {
        return getParameter(key, 0);
    }

    /**
     * @see #getParameter(String, int)
     * @deprecated Replace to <code>getParameter(String, int)</code>
     */
    @Deprecated
    public int getIntParameter(String key, int defaultValue) {
        return getParameter(key, defaultValue);
    }

    /**
     * @see #getPositiveParameter(String, int)
     * @deprecated Replace to <code>getPositiveParameter(String, int)</code>
     */
    @Deprecated
    public int getPositiveIntParameter(String key, int defaultValue) {
        return getPositiveParameter(key, defaultValue);
    }

    /**
     * @see #getParameter(String, boolean)
     * @deprecated Replace to <code>getParameter(String, boolean)</code>
     */
    @Deprecated
    public boolean getBooleanParameter(String key) {
        return getParameter(key, false);
    }

    /**
     * @see #getParameter(String, boolean)
     * @deprecated Replace to <code>getParameter(String, boolean)</code>
     */
    @Deprecated
    public boolean getBooleanParameter(String key, boolean defaultValue) {
        return getParameter(key, defaultValue);
    }

    /**
     * @see #getMethodParameter(String, String, int)
     * @deprecated Replace to <code>getMethodParameter(String, String, int)</code>
     */
    @Deprecated
    public int getMethodIntParameter(String method, String key) {
        return getMethodParameter(method, key, 0);
    }

    /**
     * @see #getMethodParameter(String, String, int)
     * @deprecated Replace to <code>getMethodParameter(String, String, int)</code>
     */
    @Deprecated
    public int getMethodIntParameter(String method, String key, int defaultValue) {
        return getMethodParameter(method, key, defaultValue);
    }

    /**
     * @see #getMethodPositiveParameter(String, String, int)
     * @deprecated Replace to <code>getMethodPositiveParameter(String, String, int)</code>
     */
    @Deprecated
    public int getMethodPositiveIntParameter(String method, String key, int defaultValue) {
        return getMethodPositiveParameter(method, key, defaultValue);
    }

    /**
     * @see #getMethodParameter(String, String, boolean)
     * @deprecated Replace to <code>getMethodParameter(String, String, boolean)</code>
     */
    @Deprecated
    public boolean getMethodBooleanParameter(String method, String key) {
        return getMethodParameter(method, key, false);
    }

    /**
     * @see #getMethodParameter(String, String, boolean)
     * @deprecated Replace to <code>getMethodParameter(String, String, boolean)</code>
     */
    @Deprecated
    public boolean getMethodBooleanParameter(String method, String key, boolean defaultValue) {
        return getMethodParameter(method, key, defaultValue);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + port;
        result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        URL other = (URL) obj;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (port != other.port)
            return false;
        if (protocol == null) {
            if (other.protocol != null)
                return false;
        } else if (!protocol.equals(other.protocol))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

}
