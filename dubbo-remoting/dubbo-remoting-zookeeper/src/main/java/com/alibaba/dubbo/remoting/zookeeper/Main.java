package com.alibaba.dubbo.remoting.zookeeper;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.zookeeper.curator.CuratorZookeeperTransporter;
import com.alibaba.dubbo.remoting.zookeeper.zkclient.ZkClientWrapper;
import com.alibaba.dubbo.remoting.zookeeper.zkclient.ZkclientZookeeperTransporter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangshiqiang on 2020-08-01.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
//        URL url = new URL("zookeeper", "127.0.0.1", 2181);
//        ZookeeperClient connect = new CuratorZookeeperTransporter().connect(url);
//        connect.create("/demo/d1", false);
//        connect.create("/demo2/d1", true);

//        List<String> children = connect.getChildren("/demo2");
//        System.out.println(children);

//        List<String> children1 = connect.getChildren("/demo");
//        System.out.println(children1);
//        connect.close();

//        ZkClientWrapper zkClientWrapper = new ZkClientWrapper("127.0.0.1:2181",1000);
//        zkClientWrapper.start();
//
//        TimeUnit.SECONDS.sleep(30);
//
//        zkClientWrapper.close();

        ZookeeperClient zookeeper = new ZkclientZookeeperTransporter().connect(new URL("zookeeper", "127.0.0.1", 2181));

        delete(zookeeper, "/demo");
        delete(zookeeper, "/demo2");
//        delete(zookeeper, "/zookeeper");

        zookeeper.close();
    }

    public static void delete(ZookeeperClient zookeeperClient, String path) {
        for (String child : zookeeperClient.getChildren(path)) {
            String x = path + "/" + child;
            System.out.println(x);
            if (zookeeperClient.getChildren(x).size() == 0) {
                zookeeperClient.delete(x);
            } else {
                delete(zookeeperClient, x);
            }
        }
        zookeeperClient.delete(path);
    }
}
