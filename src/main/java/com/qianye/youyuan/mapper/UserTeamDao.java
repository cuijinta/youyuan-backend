package com.qianye.youyuan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qianye.youyuan.model.domain.UserTeam;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户队伍关系(UserTeam)表数据库访问层
 *
 * @author makejava
 * @since 2024-03-20 10:26:41
 */
@Mapper
public interface UserTeamDao extends BaseMapper<UserTeam> {

}

