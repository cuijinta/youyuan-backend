package com.qianye.youyuan.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qianye.youyuan.common.Result;
import com.qianye.youyuan.constant.enums.ErrorCode;
import com.qianye.youyuan.exception.GlobalException;
import com.qianye.youyuan.model.domain.Team;
import com.qianye.youyuan.model.domain.User;
import com.qianye.youyuan.model.domain.UserTeam;
import com.qianye.youyuan.model.request.*;
import com.qianye.youyuan.model.vo.TeamUserVO;
import com.qianye.youyuan.model.vo.TeamVO;
import com.qianye.youyuan.service.TeamService;
import com.qianye.youyuan.service.UserService;
import com.qianye.youyuan.service.UserTeamService;
import com.qianye.youyuan.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        User logininUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        long teamId = teamService.addTeam(team,logininUser);
        return ResultUtils.success(teamId);
    }

    /**
     * 查询队伍
     * @param teamQueryRequest
     * @return
     */
    @GetMapping("/list")
    public Result<List<TeamVO>> listTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        if (teamQueryRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        // 1、查询队伍列表
        List<TeamVO> teamList = teamService.listTeams(teamQueryRequest, isAdmin);
        final List<Long> teamIdList = teamList.stream().map(TeamVO::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(teamIdList)) return ResultUtils.success(null);
        // 2、判断当前用户是否已加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
        }
        // 3、查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        return ResultUtils.success(teamList);
    }

    /**
     * 获取当前队伍
     * @param id
     * @return
     */
    @GetMapping("/get")
    public Result<Team> getTeamById(long id) {
        if (id <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
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
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(team, loginUser);
        if (!result){
            throw new GlobalException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 队长解散队伍
     * @param teamDeleteRequest
     * @return
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteTeam(@RequestBody TeamDeleteRequest teamDeleteRequest, HttpServletRequest request){
        long id = teamDeleteRequest.getId();
        if (id <= 0){
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
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
        User loginUser = userService.getLoginUser(request);
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
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户创建的队伍
     * @param request
     * @return
     */
    @GetMapping("/list/create")
    public Result<List<TeamVO>> listMyCreate(HttpServletRequest request, TeamQueryRequest teamQueryRequest) {
        if (request == null) {
            throw new GlobalException(ErrorCode.NOT_LOGIN);
        }
        User loginUser = userService.getLoginUser(request);
        teamQueryRequest.setUserId(loginUser.getId());
        List<TeamVO> teamList = teamService.listTeams(teamQueryRequest, true);

        List<Long> idList = teamList.stream().map(TeamVO::getId).collect(Collectors.toList());
        // 3、查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", idList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> {
            team.setHasJoin(true);
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size());
        });
        return ResultUtils.success(teamList);
    }

    /**
     * 获取当前用户加入的队伍
     * @param request
     * @return
     */
    @GetMapping("/list/join")
    public Result<List<TeamVO>> listMyJoinTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        if (teamQueryRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        User logininUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",logininUser.getId());
        List<UserTeam> userTeamlist = userTeamService.list(queryWrapper);

        // 取出不重复的队伍 id
        Map<Long, List<UserTeam>> listMap = userTeamlist.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
        teamQueryRequest.setIdList(idList);
        List<TeamVO> teamList = teamService.listTeams(teamQueryRequest,true);

        // 3、查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", idList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> {
            team.setHasJoin(true);
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size());
        });
        return ResultUtils.success(teamList);
    }

    /**
     * 根据teamId获取队伍完整信息
     * @param teamQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/search")
    public Result<TeamUserVO> teamQuery(@RequestBody TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        if (teamQueryRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        TeamUserVO teams = teamService.teamQuery(teamQueryRequest, request);
        return ResultUtils.success(teams);
    }

    @GetMapping("/{teamId}")
    public Result<TeamVO> getTeamById(@PathVariable("teamId") Long teamId, HttpServletRequest request) {
        if(request == null) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        TeamVO teamVO = teamService.getTeamVO(teamId, request);
        return ResultUtils.success(teamVO);
    }

}

