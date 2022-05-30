package com.atguigu.gmall.list;

import com.atguigu.gmall.list.bean.Person;
import com.atguigu.gmall.list.dao.PersonEsDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class SpringDataEsTest {

    @Autowired
    PersonEsDao personEsDao;

    /**
     * 查询住在北大街的所有人
     *
     */
    @Test
    void testQuery2() throws ParseException {
        List<Person> like = personEsDao.findAllByAddressLike("东大街");
        for (Person person : like) {
            System.out.println(person);
        }

        System.out.println("================ddd============");

        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2022-05-27 10:00");
        //中文分词的问题
        List<Person> all = personEsDao.findAllByIdGreaterThanEqualOrAddressLike(3L, "南大街");
        for (Person person : all) {
            System.out.println(person);
        }
    }


    @Test
    void testQuery(){
        Optional<Person> byId = personEsDao.findById(2L);
        System.out.println(byId.get());
    }

    @Test
    void testCrud(){
        //1、批量保存
        List<Person> asList = Arrays.asList(new Person(1L, "张三", "zhangsan@qq.com", "北京市昌平区东大街", new Date()),
                new Person(2L, "李四", "lisi@qq.com", "北京市昌平区西大街", new Date()),
                new Person(3L, "王五", "wangwu@qq.com", "北京市昌平区南大街", new Date()),
                new Person(4L, "赵六", "zhaoliu@qq.com", "北京市昌平区北大街", new Date()),
                new Person(5L, "田七", "tianqi@qq.com", "北京市昌平区中大街", new Date()));


        personEsDao.saveAll(asList);
        System.out.println("保存完成");

    }
}