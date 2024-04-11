package com.qianye.youyuan.controller;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.qianye.youyuan.common.Result;
import com.qianye.youyuan.constant.enums.ErrorCode;
import com.qianye.youyuan.exception.GlobalException;
import com.qianye.youyuan.model.domain.User;
import com.qianye.youyuan.model.request.FriendAddRequest;
import com.qianye.youyuan.model.vo.FriendsRecordVO;
import com.qianye.youyuan.service.FriendsService;
import com.qianye.youyuan.service.UserService;
import com.qianye.youyuan.utils.ResultUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * 好友申请管理表(Friends)表控制层
 *
 * @author makejava
 * @since 2024-04-03 10:26:10
 */
@RestController
@RequestMapping("/friends")
public class FriendsController {
    @Resource
    private FriendsService friendsService;

    @Resource
    private UserService userService;

    /**
     * 发起申请
     * @param friendAddRequest 请求对象
     * @param request 请求体
     * @return
     */
    @PostMapping("/add")
    public Result<Boolean> addFriendRecords(@RequestBody FriendAddRequest friendAddRequest, HttpServletRequest request) {
        if (friendAddRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "请求参数有误");
        }
        User loginUser = userService.getLoginUser(request);
        boolean addStatus = friendsService.addFriendRecords(loginUser, friendAddRequest);
        return ResultUtils.success(addStatus, "申请成功");
    }

    /**
     * 获取申请记录
     * @param request 请求体
     * @return
     */
    @GetMapping("/getRecords")
    public Result<List<FriendsRecordVO>> getRecords(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<FriendsRecordVO> friendsList = friendsService.obtainFriendApplicationRecords(loginUser);
        return ResultUtils.success(friendsList);
    }

    /**
     * 获取申请数
     * @param request 请求体
     * @return
     */
    @GetMapping("/getRecordCount")
    public Result<Integer> getRecordCount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) return ResultUtils.error(ErrorCode.NOT_LOGIN);
        int recordCount = friendsService.getRecordCount(loginUser);
        return ResultUtils.success(recordCount);
    }

    /**
     * 获取我发起的申请
     * @param request
     * @return
     */
    @GetMapping("/getMyRecords")
    public Result<List<FriendsRecordVO>> getMyRecords(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) return ResultUtils.error(ErrorCode.NOT_LOGIN);
        List<FriendsRecordVO> myFriendsList = friendsService.getMyRecords(loginUser);
        return ResultUtils.success(myFriendsList);
    }

    /**
     * 撤销申请
     * @param id 申请id
     * @param request 请求体
     * @return
     */
    @PostMapping("canceledApply/{id}")
    public Result<Boolean> canceledApply(@PathVariable("id") Long id, HttpServletRequest request) {
        if (id == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "请求参数有误");
        }
        User loginUser = userService.getLoginUser(request);
        boolean canceledApplyStatus = friendsService.canceledApply(id, loginUser);
        return ResultUtils.success(canceledApplyStatus);
    }

    /**
     * 已读申请（批量）
     * @param ids 请求id数组
     * @param request 请求体
     * @return
     */
    @GetMapping("/read")
    public Result<Boolean> toRead(@RequestParam(required = false) Set<Long> ids, HttpServletRequest request) {
        if (CollectionUtils.isEmpty(ids)) {
            return ResultUtils.success(false);
        }
        User loginUser = userService.getLoginUser(request);
        boolean isRead = friendsService.toRead(loginUser, ids);
        return ResultUtils.success(isRead);
    }

    /**
     * 同意申请
     * @param fromId 申请用户id
     * @param request 请求体
     * @return
     */
    @PostMapping("agree/{fromId}")
    public Result<Boolean> agreeToApply(@PathVariable("fromId") Long fromId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean agreeToApplyStatus = friendsService.agreeToApply(loginUser, fromId);
        return ResultUtils.success(agreeToApplyStatus);
    }


}

