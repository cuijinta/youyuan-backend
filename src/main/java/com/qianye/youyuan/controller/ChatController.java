package com.qianye.youyuan.controller;

import com.qianye.youyuan.common.Result;
import com.qianye.youyuan.constant.enums.ErrorCode;
import com.qianye.youyuan.exception.GlobalException;
import com.qianye.youyuan.model.domain.User;
import com.qianye.youyuan.model.request.ChatRequest;
import com.qianye.youyuan.model.vo.MessageVo;
import com.qianye.youyuan.service.ChatService;
import com.qianye.youyuan.service.UserService;
import com.qianye.youyuan.utils.ResultUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.qianye.youyuan.constant.ChatConstant.*;

/**
 * @Author 浅夜
 * @Description TODO
 * @DateTime 2024/4/11 16:30
 **/
@RestController
@RequestMapping("/chat")
public class ChatController {
    @Resource
    private ChatService chatService;
    @Resource
    private UserService userService;

    /**
     * 私聊
     * @param chatRequest
     * @param request
     * @return
     */
    @PostMapping("/privateChat")
    public Result<List<MessageVo>> getPrivateChat(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
        if (chatRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "请求有误");
        }
        User loginUser = userService.getLoginUser(request);
        List<MessageVo> privateChat = chatService.getPrivateChat(chatRequest, PRIVATE_CHAT, loginUser);
        return ResultUtils.success(privateChat);
    }

    @GetMapping("/hallChat")
    public Result<List<MessageVo>> getHallChat(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<MessageVo> hallChat = chatService.getHallChat(HALL_CHAT, loginUser);
        return ResultUtils.success(hallChat);
    }

    @PostMapping("/teamChat")
    public Result<List<MessageVo>> getTeamChat(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
        if (chatRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "请求有误");
        }
        User loginUser = userService.getLoginUser(request);
        List<MessageVo> teamChat = chatService.getTeamChat(chatRequest, TEAM_CHAT, loginUser);
        return ResultUtils.success(teamChat);
    }
}
