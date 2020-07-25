package com.alibaba.dubbo.demo.provider;

public class MyTest {
    public static void main(String[] args) {
        BB b = new BB();
    }
    static class BB {
        private int n = 0;
        public BB() {
            System.out.println("i am qiao" + n);
        }
        @Override
        public String toString() {
            n = 2;
            System.out.println("hello");
            return  "n == " + n;
        }
    }
}