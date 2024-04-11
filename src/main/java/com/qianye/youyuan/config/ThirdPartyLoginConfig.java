package com.qianye.youyuan.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author 浅夜
 * @Description 三方登录配置  todo：应用申请还未通过，暂时不可用
 * @DateTime 2024/4/11 13:48
 **/
@Configuration
@ConfigurationProperties(prefix = "third.config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThirdPartyLoginConfig {
    private Integer appId;
    private String appKey;
    private String redirectUrl;

    @Bean
    public ThirdPartyLoginConfig getConfig() {
        return new ThirdPartyLoginConfig(appId, appKey, redirectUrl);
    }
}
