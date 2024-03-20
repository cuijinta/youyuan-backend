package com.qianye.youyuan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qianye.youyuan.model.domain.Team;
import org.apache.ibatis.annotations.Mapper;

/**
 * 队伍(Team)表数据库访问层
 *
 * @author makejava
 * @since 2024-03-20 10:24:44
 */
@Mapper
public interface TeamDao extends BaseMapper<Team> {

}

