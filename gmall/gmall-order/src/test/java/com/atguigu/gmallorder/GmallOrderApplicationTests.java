package com.atguigu.gmallorder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
class GmallOrderApplicationTests {

    @Test
    void contextLoads() {

        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");

//        list.forEach(name -> System.out.println(name));

//        Stream<String> endWith = list.stream().filter(name -> name.endsWith("三"));
//        endWith.forEach(name-> System.out.println(name));
//        List<Integer> collect = list.stream().map(num -> Integer.parseInt(num)).collect(Collectors.toList());
    }

}
