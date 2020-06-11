package com.atguigu.gmall.ums.utils;

import java.util.Random;

public class RandomNum {

    /**
     * 生成验证码
     * @param numSize
     * @return
     */
    public static String getRandom(int numSize) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < numSize; j++) {
            int i = random.nextInt(9) + 1;
            builder.append(i);
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        String random = RandomNum.getRandom(6);
        System.out.println(random);
    }

}
