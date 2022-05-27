package com.atguigu.gmall.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Arrays;

/**
 * 自定义表达式功能
 * 文档：https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#expressions
 */
public class ExpressionTest {

    // 1、 cacheKey = "hello:#{1+1}"   代表 cacheKey = hello:2
    // 2、 cacheKey = "haha-#{redis->msg}" 代表 cacheKey = haha-world
    // 3、 cacheKey = RedisConst.HAHA + "hehe#{args[2]}"  代表 cacheKey = hahahehe第三个参数的值
    @Test
    void expressonTest(){
        //1、准备一个表达式解析器
        SpelExpressionParser parser = new SpelExpressionParser();

//        String spelStr = "hello:#{1+1}"; //hello:2
//        String spelStr = "sku:detail:#{#args[0]+'-'+#nowTime}:#{new String('hello world').toUpperCase()}====#{T(java.lang.Math).random()}";

        String spelStr = "#{new Integer(#args[0])}";
        //2、解析表达式
        TemplateParserContext parserContext = new TemplateParserContext();


        //3、表达式
        Expression expression = parser.parseExpression(spelStr, parserContext);


        StandardEvaluationContext context = new StandardEvaluationContext();
        //从上下文所有变量中找
        context.setVariable("args", Arrays.asList("22","44","55"));
        context.setVariable("nowTime",System.currentTimeMillis());


        //4、从一个上下文环境动态计算这个表达式
        Object value = expression.getValue(context, Object.class);



        System.out.println(value+":"+value.getClass());


    }
}