package com.qianye.youyuan;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@MapperScan("com.qianye.youyuan.mapper")
@SpringBootApplication
public class YouYuanApplication {

	public static void main(String[] args) {
		SpringApplication.run(YouYuanApplication.class, args);
		log.info("有缘人只找有缘人!YouYuan后端服务启动成功！");
	}

}
