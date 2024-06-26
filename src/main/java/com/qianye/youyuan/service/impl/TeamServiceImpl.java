package com.qianye.youyuan.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.youyuan.constant.enums.ErrorCode;
import com.qianye.youyuan.constant.enums.TeamStatusEnum;
import com.qianye.youyuan.exception.GlobalException;
import com.qianye.youyuan.mapper.TeamDao;
import com.qianye.youyuan.model.domain.Team;
import com.qianye.youyuan.model.domain.User;
import com.qianye.youyuan.model.domain.UserTeam;
import com.qianye.youyuan.model.request.TeamJoinRequest;
import com.qianye.youyuan.model.request.TeamQueryRequest;
import com.qianye.youyuan.model.request.TeamQuitRequest;
import com.qianye.youyuan.model.request.TeamUpdateRequest;
import com.qianye.youyuan.model.vo.TeamUserVO;
import com.qianye.youyuan.model.vo.TeamVO;
import com.qianye.youyuan.model.vo.UserVO;
import com.qianye.youyuan.service.TeamService;
import com.qianye.youyuan.service.UserService;
import com.qianye.youyuan.service.UserTeamService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.qianye.youyuan.utils.StringUtils.stringJsonListToLongSet;

/**
 * 队伍(Team)表服务实现类
 *
 * @author makejava
 * @since 2024-03-20 10:24:49
 */
@Service("teamService")
public class TeamServiceImpl extends ServiceImpl<TeamDao, Team> implements TeamService {

    @Autowired
    UserTeamService userTeamService;

    @Autowired
    UserService userService;

    @Autowired
    RedissonClient redissonClient;
    
    @Resource
    RedisTemplate redisTemplate;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空？
        if (team == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        //2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new GlobalException(ErrorCode.NOT_LOGIN);
        }
        final long userId = loginUser.getId();
        //3. 校验信息
        //  a. 队伍人数 > 1 且 <= 20
        Integer maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求");
        }
        //  b. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        //  c. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        //  d. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //  e. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        //  f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        //  g. 校验用户最多创建 5 个队伍
        // todo 有 bug，可能同时创建 100 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }
        //4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        //5. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    /**
     * 1. 从请求参数中取出队伍名称等查询条件，如果存在则作为查询条件
     * 2. 不展示已过期的队伍（根据过期时间筛选）
     * 3. 可以通过某个关键词同时对名称和描述查询
     * 4. 只有管理员才能查看加密还有非公开的房间
     * 5. 关联查询已加入队伍的用户信息
     * 6. 关联查询已加入队伍的用户信息
     *
     * @param teamQueryRequest
     * @param isAdmin
     * @return
     */
    @Override
    public List<TeamVO> listTeams(TeamQueryRequest teamQueryRequest, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 组合查询条件
        if (teamQueryRequest != null) {
            Long id = teamQueryRequest.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            List<Long> idList = teamQueryRequest.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }
            String searchText = teamQueryRequest.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            String name = teamQueryRequest.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String description = teamQueryRequest.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQueryRequest.getMaxNum();
            // 查询最大人数相等的
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamQueryRequest.getUserId();
            // 根据创建人来查询
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            if(teamQueryRequest.getStatus() != null) {
                // 根据状态来查询
                Integer status = teamQueryRequest.getStatus();
                TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
                if (statusEnum == null) {
                    statusEnum = TeamStatusEnum.PUBLIC;
                }
                if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                    throw new GlobalException(ErrorCode.NO_AUTH);
                }
                queryWrapper.eq("status", statusEnum.getValue());
            }
        }
        // 不展示已过期的队伍
        // expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        queryWrapper.orderByDesc("createTime");
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamVO> teamVOList = new ArrayList<>();
        // 关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamVO teamVO = new TeamVO();
            BeanUtils.copyProperties(team, teamVO);
            // 脱敏用户信息
            if (user != null) {
//                UserVO userVO = new UserVO();
//                BeanUtils.copyProperties(user, userVO);
                teamVO.setUser(userService.getSafetyUser(user));
            }
            Set<User> userSet = new HashSet<>();
            LambdaQueryWrapper<UserTeam> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(UserTeam::getTeamId, team.getId()).select(UserTeam::getUserId);
            List<UserTeam> userIdList = userTeamService.list(queryWrapper1);
            if(CollectionUtils.isNotEmpty(userIdList)) {
                userIdList.forEach(userTeam -> {
                    userSet.add(userService.getSafetyUser(userService.getById((userTeam.getUserId()))));
                });
            }
            teamVO.setUserSet(userSet);
            teamVOList.add(teamVO);
        }
        return teamVOList;

    }

    /**
     * 修改队伍信息
     * 1. 判断请求参数是否为空
     * 2. 查询队伍是否存在
     * 3. 只有管理员或者队伍的创建者可以修改
     * 4. 如果用户传入的新值和老值一致，就不用 update 了
     * 5. 如果队伍状态改为加密，必须要有密码
     * 6. 更新成功
     *
     * @param team
     * @return
     */
    @Override
    public boolean updateTeam(TeamUpdateRequest team, User loginUser) {
        if (team == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        Long id = team.getId();
        if (id == null || id <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 只有管理员或者队伍的创建者可以修改
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(team.getStatus());
        if (statusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StringUtils.isBlank(team.getPassword())) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "加密房间必须要设置密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(team, updateTeam);
        return this.updateById(updateTeam);
    }

    @Transactional
    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null){
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null || teamId <= 0){
            throw  new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null){
            throw  new GlobalException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())){
            throw new GlobalException(ErrorCode.PARAMS_ERROR,"队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){
            throw new GlobalException(ErrorCode.PARAMS_ERROR,"禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())){
                throw new GlobalException(ErrorCode.PARAMS_ERROR,"密码错误");
            }
        }
        //该用户已加入的队伍数量
        long userId = loginUser.getId();

        //只有一个线程能获得锁
        RLock lock = redissonClient.getLock("youyuan:join_team");
        try {
            //抢到锁并执行
            while(true) {
                if(lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    System.out.println("getLock: " + Thread.currentThread().getId());
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId",userId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if (hasJoinNum > 5){
                        throw new GlobalException(ErrorCode.PARAMS_ERROR,"最多创建和加入5个队伍");
                    }
                    //不能重复加入已加入的队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId",userId);
                    userTeamQueryWrapper.eq("teamId",teamId);
                    long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if (hasUserJoinTeam > 0){
                        throw new GlobalException(ErrorCode.PARAMS_ERROR,"用户已加入该队伍");
                    }
                    //已加入队伍的人数
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("teamId",teamId);
                    long teamHasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if (teamHasJoinNum >= team.getMaxNum()){
                        throw new GlobalException(ErrorCode.PARAMS_ERROR,"队伍已满");
                    }
                    //加入，修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        }catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
            return false;
        } finally {
            // 释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    /**
     * 用户退出队伍
     * 1.  校验请求参数
     * 2.  校验队伍是否存在
     * 3.  校验我是否已加入队伍
     * 4.  如果队伍
     *   a.  只剩一人，队伍解散
     *   b.  还有其他人
     *    ⅰ.  如果是队长退出队伍，权限转移给第二早加入的用户 —— 先来后到 只用取 id 最小的 2 条数据
     *    ⅱ.  非队长，自己退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null){
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        if (teamId == null || teamId <= 0){
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null){
            throw new GlobalException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setUserId(userId);
        queryUserTeam.setTeamId(teamId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0 ){
            throw new GlobalException(ErrorCode.PARAMS_ERROR,"未加入队伍");
        }
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        // 队伍只剩一人，解散
        if (teamHasJoinNum == 1) {
            // 删除队伍
            this.removeById(teamId);
        } else {
            // 队伍还剩至少两人
            // 是队长
            if (team.getUserId() == userId) {
                // 把队伍转移给最早加入的用户
                // 1. 查询已加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new GlobalException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                // 更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new GlobalException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        // 移除关系
        return userTeamService.remove(queryWrapper);
    }

    /**
     * 队长解散队伍
     * 1. 校验请求参数
     * 2. 校验队伍是否存在
     * 3. 校验你是不是队伍的队长
     * 4. 移除所有加入队伍的关联信息
     * 5. 删除队伍
     * @param id
     * @param loginUser
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteTeam(long id, User loginUser) {
        // 校验队伍是否存在
        Team team = getById(id);
        long teamId = team.getId();
        // 校验你是不是队伍的队长
        if (!team.getUserId().equals(loginUser.getId())) {
            throw new GlobalException(ErrorCode.NO_AUTH, "无权解散！");
        }
        // 移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result) {
            throw new GlobalException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        // 删除队伍
        return this.removeById(teamId);
    }

    /**
     * 查当前用户创建的队伍
     * @param teamQueryRequest
     * @return
     */
    @Override
    public List<TeamVO> listTeams(TeamQueryRequest teamQueryRequest) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 组合查询条件
        if (teamQueryRequest != null) {
            Long id = teamQueryRequest.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            //因为上面是拿的vo,所以这里需要添加
            List<Long> idList = teamQueryRequest.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }
            String searchText = teamQueryRequest.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            String name = teamQueryRequest.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String description = teamQueryRequest.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQueryRequest.getMaxNum();
            // 查询最大人数相等的
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamQueryRequest.getUserId();
            // 根据创建人来查询
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 根据状态来查询
            Integer status = teamQueryRequest.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
//            if (statusEnum == null) {
//                statusEnum = TeamStatusEnum.PUBLIC;
//            }
//            queryWrapper.eq("status", statusEnum.getValue());
        }

        // 不展示已过期的队伍
        // expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }

        List<TeamVO> teamVOList = new ArrayList<>();
        // 关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamVO teamVO = new TeamVO();
            BeanUtils.copyProperties(team, teamVO);
            // 脱敏用户信息
            if (user != null) {
//                UserVO userVO = new UserVO();
//                BeanUtils.copyProperties(user, userVO);
                teamVO.setUser(user);
            }
            teamVOList.add(teamVO);
        }
        return teamVOList;
    }

    @Override
    public List<TeamVO> listTeam(long userId) {
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        if (org.springframework.util.CollectionUtils.isEmpty(userTeamList)) return null;
        List<Long> teamIds = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toList());
        QueryWrapper<Team> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.in("id", teamIds);
        List<Team> teamList = list(queryWrapper1);
        if (CollectionUtils.isEmpty(teamList)) return null;

        List<TeamVO> teamVOList = new ArrayList<>();
        for(Team team : teamList) {
            TeamVO teamVO = new TeamVO();
            BeanUtils.copyProperties(team, teamVO);
            teamVOList.add(teamVO);
        }
        return teamVOList;
    }

    @Override
    public TeamUserVO teamQuery(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        userService.isLogin(request);
        String searchText = teamQueryRequest.getSearchText();
        String teamQueryKey = String.format("youyuan:team:teamQuery:%s", searchText);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        TeamUserVO teamList = (TeamUserVO) valueOperations.get(teamQueryKey);
        if (teamList != null) {
            return teamList;
        }
        LambdaQueryWrapper<Team> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teamLambdaQueryWrapper.like(Team::getDescription, searchText.trim())
                .or().like(Team::getName, searchText.trim());
        List<Team> teams = this.list(teamLambdaQueryWrapper);
        // 过滤后的队伍列表
        TeamUserVO teamUserVO = teamSet(teams);
        setRedis(teamQueryKey, teamUserVO);
        return teamUserVO;
    }

    /**
     * 根据队伍id获取队伍完整信息
     * @param teamId
     * @param request
     * @return
     */
    @Override
    public TeamVO getTeamVO(Long teamId, HttpServletRequest request) {
        TeamVO teamVO = new TeamVO();
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Team::getId, teamId);
        Team team = getOne(queryWrapper);
        if(team == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        BeanUtils.copyProperties(team, teamVO);
        if(team.getUserId() != null) {
            teamVO.setUser(userService.getById(team.getUserId()));
        }

        Set<User> userSet = new HashSet<>();
        LambdaQueryWrapper<UserTeam> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(UserTeam::getTeamId, teamId).select(UserTeam::getUserId);
        List<UserTeam> userTeams = userTeamService.list(queryWrapper1);
        if(CollectionUtils.isNotEmpty(userTeams)) {
            Set<Long> userIds = userTeams.stream().map(UserTeam::getUserId).collect(Collectors.toSet());
            userIds.forEach(userId -> {
                userSet.add(userService.getSafetyUser(userService.getById(userId)));
            });
        }
        teamVO.setUserSet(userSet);

        return teamVO;
    }

    /**
     * 处理返回信息Vo
     *
     * @param teamList
     * @return TeamUserVO
     */
    public TeamUserVO teamSet(List<Team> teamList) {
        // 过滤过期的队伍
        List<Team> listTeam = teamList.stream()
                .filter(team -> !new Date().after(team.getExpireTime()))
                .collect(Collectors.toList());
        Collections.shuffle(listTeam);
        TeamUserVO teamUserVO = new TeamUserVO();
        Set<TeamVO> users = new HashSet<>();
        listTeam.forEach(team -> {
            TeamVO teamVo = new TeamVO();
            teamVo.setId(team.getId());
            teamVo.setName(team.getName());
            teamVo.setTeamAvatarUrl(team.getTeamAvatarUrl());
            teamVo.setDescription(team.getDescription());
            teamVo.setMaxNum(team.getMaxNum());
            teamVo.setExpireTime(team.getExpireTime());
            teamVo.setStatus(team.getStatus());
            teamVo.setCreateTime(team.getCreateTime());
            teamVo.setAnnounce(team.getAnnounce());
//            Set<Long> userSet = stringJsonListToLongSet(usersId);
            Set<User> userSet = new HashSet<>();
            LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserTeam::getTeamId, team.getId());
            List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
            if(CollectionUtils.isNotEmpty(userTeamList)) {
                userTeamList.forEach(userTeam -> {
                    userSet.add(userService.getSafetyUser(userService.getById(userTeam.getUserId())));
                });
            }
//            for (Long id : userSet) {
//                userList.add(userService.getById(id));
//            }
            User createUser = userService.getById(team.getUserId());
            User safetyUser = userService.getSafetyUser(createUser);
            teamVo.setUser(safetyUser);
//            userList = userList.stream().map(userService::getSafetyUser).collect(Collectors.toSet());
            teamVo.setUserSet(userSet);
            users.add(teamVo);
        });
        teamUserVO.setTeamSet(users);
        return teamUserVO;
    }

    /**
     * 设置 redis 3分钟
     *
     * @param redisKey
     * @param data
     */
    private void setRedis(String redisKey, Object data) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        try {
            // 解决缓存雪崩
            int i = RandomUtil.randomInt(1, 2);
            valueOperations.set(redisKey, data, 1 + i / 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis set key error");
        }
    }

    /**
     * 查询当前队伍人数
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(Long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }
}

