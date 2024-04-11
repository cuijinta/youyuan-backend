package com.qianye.youyuan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qianye.youyuan.model.domain.Team;
import com.qianye.youyuan.model.domain.User;
import com.qianye.youyuan.model.request.TeamJoinRequest;
import com.qianye.youyuan.model.request.TeamQueryRequest;
import com.qianye.youyuan.model.request.TeamQuitRequest;
import com.qianye.youyuan.model.request.TeamUpdateRequest;
import com.qianye.youyuan.model.vo.TeamUserVO;
import com.qianye.youyuan.model.vo.TeamVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 队伍(Team)表服务接口
 *
 * @author makejava
 * @since 2024-03-20 10:24:48
 */
public interface TeamService extends IService<Team> {

    /**
     * 新增队伍
     * @param team
     * @param logininUser
     * @return
     */
    long addTeam(Team team, User logininUser);

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
    List<TeamVO> listTeams(TeamQueryRequest teamQueryRequest, boolean isAdmin);

    /**
     * 修改队伍信息
     *
     * @param team
     * @return
     */
    boolean updateTeam(TeamUpdateRequest team, User loginUser);

    /**
     * 加入队伍
     * 其他人、未满、未过期，允许加入多个队伍，但是要有个上限
     * 1. 用户最多加入 5 个队伍
     * 2. 队伍必须存在，只能加入未满、未过期的队伍
     * 3. 不能加入自己的队伍，不能重复加入已加入的队伍（幂等性）
     * 4. 禁止加入私有的队伍
     * 5. 如果加入的队伍是加密的，必须密码匹配才可以
     * 6. 新增 队伍 - 用户 关联信息
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

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
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

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
    boolean deleteTeam(long id, User loginUser);

    /**
     * 查当前用户创建的队伍
     * @param teamQueryRequest
     * @return
     */
    List<TeamVO> listTeams(TeamQueryRequest teamQueryRequest);

    /**
     * 查询当前用户加入的队伍
     * @param userId
     * @return
     */
    List<TeamVO> listTeam(long userId);

    TeamUserVO teamQuery(TeamQueryRequest teamQueryRequest, HttpServletRequest request);
}

