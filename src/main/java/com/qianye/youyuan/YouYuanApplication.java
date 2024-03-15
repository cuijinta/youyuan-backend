package com.qianye.youyuan;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@MapperScan("com.qianye.youyuan.mapper")
@SpringBootApplication
@EnableScheduling
public class YouYuanApplication {

	public static void main(String[] args) {
		SpringApplication.run(YouYuanApplication.class, args);
		System.setProperty("spring.devtools.restart.enabled", "false"); // 关闭热部署
		log.info("有缘人只找有缘人!YouYuan后端服务启动成功！");
	}

}
