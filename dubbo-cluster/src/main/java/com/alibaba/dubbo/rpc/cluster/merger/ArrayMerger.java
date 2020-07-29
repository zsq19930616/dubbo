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
package com.alibaba.dubbo.rpc.cluster.merger;

import com.alibaba.dubbo.rpc.cluster.Merger;

import java.lang.reflect.Array;

public class ArrayMerger implements Merger<Object[]> {

    public static void main(String[] args) {
        ArrayMerger arrayMerger = new ArrayMerger();
        Object[][] objects = new Object[2][3];
        for (int i = 0; i < objects.length; i++) {
            for (int j = 0; j < objects.length; j++) {
                objects[i][j] = new Object();
            }
        }
        Object[] merge = arrayMerger.merge(objects);
        System.out.println(merge.length);


        Object[] o1 = {new Object(),new Object()};
        Object[] o2 = {new Object(),new Object()};
        Object[] merge1 = arrayMerger.merge(o1, o2);
        System.out.println(merge1.length);
    }

    // 默认的
    public static final ArrayMerger INSTANCE = new ArrayMerger();

    // 数组合并返回，按照顺序
    public Object[] merge(Object[]... others) {
        if (others.length == 0) {
            return null;
        }
        int totalLen = 0;
        for (int i = 0; i < others.length; i++) {
            Object item = others[i];
            if (item != null && item.getClass().isArray()) {
                totalLen += Array.getLength(item);
            } else {
                throw new IllegalArgumentException(
                        new StringBuilder(32).append(i + 1)
                                .append("th argument is not an array").toString());
            }
        }

        if (totalLen == 0) {
            return null;
        }

        Class<?> type = others[0].getClass().getComponentType();

        // 创建对应类型的数组
        Object result = Array.newInstance(type, totalLen);
        int index = 0;
        for (Object array : others) {
            for (int i = 0; i < Array.getLength(array); i++) {
                Array.set(result, index++, Array.get(array, i));
            }
        }
        return (Object[]) result;
    }

}