package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.list.dao.GoodsDao;
import com.atguigu.gmall.list.service.GoodsSearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.vo.GoodsSearchResultVo;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GoodsSearchServiceImpl implements GoodsSearchService {

   @Autowired
    GoodsDao goodsDao;

    @Autowired
    ElasticsearchRestTemplate restTemplate;

    @Override
    public void saveGoods(Goods goods) {
        goodsDao.save(goods);

    }

    @Override
    public void deleteGoods(Long skuId) {
        goodsDao.deleteById(skuId);
    }

    @Override
    public GoodsSearchResultVo search(SearchParam param) {

        //0、根据前端传递来的参数，构造复杂的检索条件
        Query query = buildQueryBySearchParam(param); //封装了复杂的检索逻辑

        //1、检索
        SearchHits<Goods> hits = restTemplate.search(query, Goods.class, IndexCoordinates.of("goods"));

        //2、数据提取
        GoodsSearchResultVo resultVo = buildResponse(hits);
        return resultVo;
    }



    //根据前端传递的复杂检索条件，构造自己的Query条件
    //建议打开追踪器，看下这个 Query 对应的DSL到底是什么东西
    private Query buildQueryBySearchParam(SearchParam param) {

        //0、构建一个bool query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //==========构造查询开始=================
        //1、按照三级分类查询bool- must - category3Id(term)
        if(param.getCategory3Id() != null){
            boolQuery.must(QueryBuilders.termQuery("category3Id",param.getCategory3Id()));
        }
        if(param.getCategory2Id() != null){
            boolQuery.must(QueryBuilders.termQuery("category2Id",param.getCategory2Id()));
        }
        if (param.getCategory1Id() != null){
            boolQuery.must(QueryBuilders.termQuery("category1Id",param.getCategory1Id()));
        }

        //2. 按照品牌查询  trademark=1:小米
        if (!StringUtils.isEmpty(param.getTrademark())){
            String[] split = param.getTrademark().split(":");
            boolQuery.must(QueryBuilders.termQuery("tmId",split[0]));
        }

        //3. 按照商品名进行模糊检索、全文匹配
        if (!StringUtils.isEmpty(param.getKeyword())){
            boolQuery.must(QueryBuilders.matchQuery("title",param.getKeyword()));
        }

        //TODO 根据param完善 Query中的其他条件

        //代表完整的检索条件
        NativeSearchQuery query = new NativeSearchQuery(boolQuery); //query -

        return query;
    }

    //根据检索结果构造响应数据
    private GoodsSearchResultVo buildResponse(SearchHits<Goods> hits) {
        return null;

    }
}
