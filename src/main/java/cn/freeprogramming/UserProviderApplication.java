package cn.freeprogramming;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author humorchen
 * @date 2021/12/6 1:06
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("cn.freeprogramming.mapper")
public class UserProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserProviderApplication.class,args);
    }
}
