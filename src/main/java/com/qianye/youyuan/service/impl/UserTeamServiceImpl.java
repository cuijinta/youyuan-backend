package com.qianye.youyuan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.youyuan.mapper.UserTeamDao;
import com.qianye.youyuan.model.domain.UserTeam;
import com.qianye.youyuan.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
 * 用户队伍关系(UserTeam)表服务实现类
 *
 * @author makejava
 * @since 2024-03-20 10:26:44
 */
@Service("userTeamService")
public class UserTeamServiceImpl extends ServiceImpl<UserTeamDao, UserTeam> implements UserTeamService {

}

