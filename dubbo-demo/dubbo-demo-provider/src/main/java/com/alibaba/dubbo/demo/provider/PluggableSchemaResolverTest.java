package com.alibaba.dubbo.demo.provider;

import org.springframework.beans.factory.xml.PluggableSchemaResolver;

/**
 * @author zhangshiqiang on 2020-07-25.
 */
public class PluggableSchemaResolverTest {
    public static void main(String[] args) {
        PluggableSchemaResolverChild pluggableSchemaResolver = new PluggableSchemaResolverChild(ClassLoader.getSystemClassLoader());
        System.out.println(pluggableSchemaResolver);
    }

    public static class PluggableSchemaResolverChild extends PluggableSchemaResolver{
        public PluggableSchemaResolverChild(ClassLoader classLoader) {
            super(classLoader);
        }

        @Override
        public String toString() {
            return "PluggableSchemaResolverChild{}";
        }
    }
}
