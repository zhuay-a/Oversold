package com.example.thread_safe_online.service;

import com.example.thread_safe_online.entry.dto.GoodsDTO;
import com.example.thread_safe_online.entry.result.Result;

public interface GoodsService {
    Result<String> buyByIdLock(int id);

    Result<String> buyByIdCAS(int id);

    Result<String> addGoods(GoodsDTO goodsDTO);

    Result<String> buyById(int id);

    Result<String> buyByIdRedis(int id);

    Result<String> updateGoodsById(GoodsDTO goodsDTO);

    Result<String> buyByIdDLock(int id);

    Result<String> buyByIdRedisson(int id);
}
