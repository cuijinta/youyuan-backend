package com.qianye.youyuan.controller;

import com.qianye.youyuan.service.UserTeamService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 用户队伍关系(UserTeam)表控制层
 *
 * @author makejava
 * @since 2024-03-20 10:26:40
 */
@RestController
@RequestMapping("userTeam")
public class UserTeamController {
    /**
     * 服务对象
     */
    @Resource
    private UserTeamService userTeamService;


}

