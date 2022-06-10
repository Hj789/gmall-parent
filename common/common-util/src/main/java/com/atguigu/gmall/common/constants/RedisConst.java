package com.atguigu.gmall.common.constants;

import java.time.Duration;

public class RedisConst {

    public static final String CATEGORY_CACHE_KEY = "categorys";
    public static final String SKU_CACHE_KEY_PREFIX = "sku:detail:"; //sku:detail:49
    public static final String BOOM_SKU_ID = "bloom:skuid"; // "bloom:order"
    public static final String LOCK_PREFIX = "lock:"; //lock: 50
    public static final String SKUDETAIL_LOCK_PREFIX = "lock:detail:"; //lock:detail: 50
    public static final String SALE_ATTR_CACHE_KEY = "sale:attr:";
    public static final String SKU_HOTSCORE = "sku:hotScore";
    public static final String USER_LOGIN_PREFIX = "user:login:";
    public static final String CART_KEY_PREFIX = "user:cart:";
    public static final Duration TEMP_CART_TIMEOUT = Duration.ofDays(90); //3个月
    public static final Long CART_SIZE_LIMIT = 200L;
    public static final String NO_REPEAT_TOKEN = "norepeat:token:";

}

