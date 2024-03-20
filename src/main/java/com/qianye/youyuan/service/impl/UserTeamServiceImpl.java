package com.qianye.youyuan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.youyuan.mapper.UserTeamDao;
import com.qianye.youyuan.model.domain.Team;
import com.qianye.youyuan.model.domain.UserTeam;
import com.qianye.youyuan.model.vo.TeamUserVO;
import com.qianye.youyuan.service.TeamService;
import com.qianye.youyuan.service.UserTeamService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户队伍关系(UserTeam)表服务实现类
 *
 * @author makejava
 * @since 2024-03-20 10:26:44
 */
@Service("userTeamService")
public class UserTeamServiceImpl extends ServiceImpl<UserTeamDao, UserTeam> implements UserTeamService {

}

