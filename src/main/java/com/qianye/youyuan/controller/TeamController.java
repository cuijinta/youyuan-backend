package com.qianye.youyuan.controller;

import com.qianye.youyuan.common.Result;
import com.qianye.youyuan.constant.ErrorCode;
import com.qianye.youyuan.exception.GlobalException;
import com.qianye.youyuan.model.domain.Team;
import com.qianye.youyuan.model.domain.User;
import com.qianye.youyuan.model.request.*;
import com.qianye.youyuan.model.vo.TeamUserVO;
import com.qianye.youyuan.service.TeamService;
import com.qianye.youyuan.service.UserService;
import com.qianye.youyuan.service.UserTeamService;
import com.qianye.youyuan.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 队伍(Team)表控制层
 *
 * @author makejava
 * @since 2024-03-20 10:24:43
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173/"})
@Slf4j
public class TeamController {
    /**
     * 服务对象
     */
    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 新增队伍
     * @param teamAddRequest 请求对象
     * @param request http请求
     * @return
     */
    @PostMapping("/add")
    public Result<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if (teamAddRequest == null){
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        User logininUser = userService.getLogininUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        long teamId = teamService.addTeam(team,logininUser);
        return ResultUtils.success(teamId);
    }

    /**
     * 查询队伍
     * @param teamQuery
     * @return
     */
    @GetMapping("/list")
    public Result<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        return ResultUtils.success(teamList);
    }

    /**
     * 修改队伍信息
     *
     * @param team
     * @return
     */
    @PostMapping("/update")
    public Result<Boolean> updateTeam(@RequestBody TeamUpdateRequest team, HttpServletRequest request){
        if (team == null){
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLogininUser(request);
        boolean result = teamService.updateTeam(team, loginUser);
        if (!result){
            throw new GlobalException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 队长解散队伍
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteTeam(@RequestBody long id, HttpServletRequest request){
        if (id <= 0){
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLogininUser(request);
        boolean result = teamService.deleteTeam(id, loginUser);
        if (!result){
            throw new GlobalException(ErrorCode.SYSTEM_ERROR,"解散失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param request
     * @return
     */
    @PostMapping("/join")
    public Result<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request){
        if(teamJoinRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLogininUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 用户退出队伍
     * @param teamQuitRequest
     * @param request
     * @return
     */
    @PostMapping("/quit")
    public Result<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if (teamQuitRequest == null){
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLogininUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户创建的队伍
     * @param request
     * @return
     */
    @GetMapping("/list/myTeams")
    public Result<List<TeamUserVO>> listMyTeams(HttpServletRequest request, TeamQuery teamQuery) {
        if (request == null) {
            throw new GlobalException(ErrorCode.NOT_LOGIN);
        }
        User loginUser = userService.getLogininUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery);
        return ResultUtils.success(teamList);
    }

    /**
     * 获取当前用户加入的队伍
     * @param request
     * @return
     */
    @GetMapping("/list/teams")
    public Result<List<TeamUserVO>> listTeams(HttpServletRequest request) {
        if (request == null) {
            throw new GlobalException(ErrorCode.NOT_LOGIN);
        }
        long userId = 0L;
        User loginUser = userService.getLogininUser(request);
        if(loginUser != null) userId = loginUser.getId();
        List<TeamUserVO> teamList = teamService.listTeam(userId);
        return ResultUtils.success(teamList);
    }
}

