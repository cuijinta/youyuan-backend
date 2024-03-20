package com.qianye.youyuan.service;

import com.qianye.youyuan.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @Author 浅夜
 * @Description TODO
 * @DateTime 2024/3/12 0:27
 **/
@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserService userService;

    //线程设置
    private ExecutorService executorService = new ThreadPoolExecutor(16, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**
     * 循环插入用户  耗时：7260ms
     * 批量插入用户   1000  耗时： 2215ms
     */
    @Test
    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch(); //spring提供的计时工具
        stopWatch.start();
        final int INSERT_NUM = 1000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("mock" + i);
            user.setUserAccount("mockAccount" + i);
            user.setAvatarUrl("https://img0.baidu.com/it/u=3514514443,3153875602&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("18719191919");
            user.setEmail("12138@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setCode("22562");
            user.setTags("[]");
            user.setProfile("这是一段对于第" + i + "个假数据的简介");
            userList.add(user);
        }
        userService.saveBatch(userList, 100);
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());

    }

    /**
     * 并发批量插入用户   100000  耗时： 23887ms
     */
    @Test
    public void doConcurrencyInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 10000;
        // 分十组
        int j = 0;
        //批量插入数据的大小
        int batchSize = 500;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        // i 要根据数据量和插入批量来计算需要循环的次数。
        for (int i = 0; i < INSERT_NUM / batchSize; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUsername("假数据" + j);
                user.setUserAccount("qianye" + j);
                user.setAvatarUrl("http://s94tz8lzk.hd-bkt.clouddn.com/2024/0304/9fc410baeecc466799e78ac7852cbb87.jpg");
                user.setProfile("这是一段关于" + user.getUsername() + "的简介");
                user.setGender(1);
                user.setUserPassword("f263c182a55a231f7cd899eb76d23b13");
                user.setPhone("123456789108");
                user.setEmail("22288999@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setCode("33322");
                user.setTags("[]");
                userList.add(user);
                if (j % batchSize == 0) {
                    break;
                }
            }
            //异步执行 使用CompletableFuture开启异步任务
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreadName：" + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }
}
