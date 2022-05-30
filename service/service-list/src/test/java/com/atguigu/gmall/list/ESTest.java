package com.atguigu.gmall.list;

import com.atguigu.gmall.common.util.JSONs;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class ESTest {


    @Autowired
    ElasticsearchRestTemplate restTemplate;


    @Test
    void testQuery(){
        //1、单个查
        Hello hello = restTemplate.get("1", Hello.class, IndexCoordinates.of("lei"));
        System.out.println(hello);

        //2、复杂查。id>10
        //Query var1, 查询条件
        // Class<T> var2, 数据的结果类型
        // IndexCoordinates var3 索引
        SearchHits<Hello> lei = restTemplate.search(Query.findAll(), Hello.class, IndexCoordinates.of("lei"));

        //获取所有命中的记录
        List<SearchHit<Hello>> searchHits = lei.getSearchHits();
        for (SearchHit<Hello> searchHit : searchHits) {
            float score = searchHit.getScore();
            String id = searchHit.getId();
            Hello content = searchHit.getContent();
            System.out.println(score+"=="+id+"=="+content);
        }

    }

    @Test
    void testUpdate(){
        Hello hello = new Hello(1L, "eee", null);
        String toStr = JSONs.toStr(hello);
        System.out.println(toStr);
//        Document document = Document.parse(toStr);


        Map<String,Object> map = new HashMap<>();
        map.put("id",1L);
        map.put("name","dhhd");
        //只带需要修改的字段
        Document document = Document.from(map);

        //更新query
        UpdateQuery query = UpdateQuery.builder("1")
                .withDocAsUpsert(false)
                .withDocument(document)
                .build();

        UpdateResponse lei = restTemplate.update(query, IndexCoordinates.of("lei"));
        System.out.println(lei.getResult());
    }

    @Test
    void testDelete(){
        String lei = restTemplate.delete("1", IndexCoordinates.of("lei"));
        System.out.println(lei);
    }


    @Test
    void testIndex(){
        //保存
//        restTemplate.index()
        /**
         * IndexQuery query,
         * IndexCoordinates index
         */
        IndexQuery query = new IndexQuery();
        Hello hello = new Hello(1L, "world", "李四");
        query.setId(hello.getId().toString()); //指定用的id
        query.setObject(hello); //指定要保存哪个对象

        //保存操作
        String id = restTemplate.index(query, IndexCoordinates.of("lei"));
        System.out.println(id);
    }
}


    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    class Hello{
        private Long id;
        private String msg;
        private String name;
    }


