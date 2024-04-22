package com.example.thread_safe_online.dao;

import com.example.thread_safe_online.entry.dto.GoodsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GoodsDao {

    @Update("update goods set num = num - 1 where id = #{id}")
    void buyByIdLock(int id);

    int addGoods(GoodsDTO goodsDTO);

    @Select("select num from goods where id = #{id}")
    int getNumById(int id);

    @Update("update goods set num = num - 1 where id = #{id} and num > 0")
    int buyByIdCAS(int id);

    int updateById(GoodsDTO goodsDTO);

    @Select("select * from goods where id = #{id}")
    GoodsDTO getById(Integer id);

    @Update("update goods set num = num - 1 where ii = #{ii}")
    void errorGet();
}
