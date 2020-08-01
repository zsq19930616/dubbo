package com.alibaba.dubbo.remoting.test;

import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Transporters;
import com.alibaba.dubbo.remoting.transport.ChannelHandlerAdapter;

public class TelnetServer {

    public static void main(String[] args) throws Exception {
        Transporters.bind("telnet://0.0.0.0:23", new ChannelHandlerAdapter() {
            public void connected(Channel channel) throws RemotingException {
                channel.send("telnet> ");
            }

            public void received(Channel channel, Object message) throws RemotingException {
                channel.send("Echo: " + message + "\r\n");
                channel.send("telnet> ");
            }
        });
        // Prevent JVM from exiting
        synchronized (TelnetServer.class) {
            while (true) {
                try {
                    TelnetServer.class.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

}