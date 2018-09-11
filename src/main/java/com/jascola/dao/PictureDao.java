package com.jascola.dao;

import com.jascola.dto.PicQueryDto;
import com.jascola.entity.PicturesEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PictureDao {

    int insert(PicturesEntity entity);

    List<PicturesEntity> selectByPicName(String picname);

    List<PicturesEntity> selectByAuName(PicQueryDto dto);

    int deleteByPicName(String picname);

    int deleteByAuName(String authorname);

    int update(PicturesEntity entity);

    List<PicturesEntity> selectAll(PicQueryDto dto);

    List<PicturesEntity> selectById(Integer id);

    Integer selectCount();

    Integer selectCountByAuName(String authorname);
}
