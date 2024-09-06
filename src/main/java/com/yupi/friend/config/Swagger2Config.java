package com.yupi.friend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger2配置信息
 * 这里分了两组显示
 * 第一组是api，当作用户端接口
 * 第二组是admin，当作后台管理接口
 * 也可以根据实际情况来减少或者增加组
 *
 */

@Configuration
@EnableSwagger2WebMvc
@Profile({"dev"})
public class Swagger2Config {

    private ApiInfo webApiInfo() {
        return new ApiInfoBuilder()
                .title("伙伴匹配系统-API文档")
                .description("xxxx")
                .version("1.0")
                .contact(new Contact("sheephappy", "https://bilibili.com", "xxx"))
                .build();
    }

    /**
     * 第一组：api
     * @return
     */
    @Bean
    public Docket webApiConfig() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new ParameterBuilder()
                .name("Authorization")
                .description("Bearer Token")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build());

        Docket webApi = new Docket(DocumentationType.SWAGGER_2)
                .groupName("用户端接口")
                .apiInfo(webApiInfo())
                .select()
                //只显示api路径下的页面
                .apis(RequestHandlerSelectors.basePackage("com.yupi.friend.controller"))
                .paths(PathSelectors.regex("/api/.*"))
                .build()
                .globalOperationParameters(parameters);


        return webApi;
    }

//    /**
//     * 第二组：admin
//     * @return
//     */
//
//
//
//    private ApiInfo adminApiInfo() {
//        return new ApiInfoBuilder()
//                .title("Eric-SpringBoot整合Knife4j-API文档")
//                .description("本文档描述了SpringBoot如何整合Knife4j")
//                .version("1.0")
//                .contact(new Contact("Eric", "https://blog.csdn.net/weixin_47316183?type=blog", "ericsyn@foxmail.com"))
//                .build();
//    }
//    @Bean
//    public Docket adminApiConfig() {
//        List<Parameter> pars = new ArrayList<>();
//        ParameterBuilder tokenPar = new ParameterBuilder();
//        tokenPar.name("adminId")
//                .description("用户token")
//                .defaultValue("1")
//                .modelRef(new ModelRef("string"))
//                .parameterType("header")
//                .required(false)
//                .build();
//        pars.add(tokenPar.build());
//
//        Docket adminApi = new Docket(DocumentationType.SWAGGER_2)
//                .groupName("后台接口")
//                .apiInfo(adminApiInfo())
//                .select()
//                //只显示admin路径下的页面
//                .apis(RequestHandlerSelectors.basePackage("com.eric.springbootknife4j"))
//                .paths(PathSelectors.regex("/admin/.*"))
//                .build()
//                .globalOperationParameters(pars);
//        return adminApi;
//    }

}

