package com.hua.community.config;

import com.hua.community.util.CommunityConstant;
import com.hua.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @create 2022-05-08 15:53
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    /**
     * 配置不拦截的路径
     *
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    /**
     * 授权
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //授权
        http.authorizeRequests()
                .antMatchers(
                        "/comment/add/**",  //发表评论
                        "/discuss/add", //发布帖子
                        "/follow",  //关注
                        "/like",    //点赞
                        "/unfollow",    //取消关注
                        "/letter/**",   //私信
                        "/notice/**",   //系统通知
                        "/user/setting",    //账号设置页面
                        "/user/upload"  //头像上传页面
                ).hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/top",     //置顶帖子功能
                        "/discuss/wonderful"     //设置精华功能
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/delete",    //删除贴子功能
                        "/data/**"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()      //除了上面配置的路径，其他请求都放行
                .and().csrf().disable(); //关闭csrf

        //权限不够时的处理
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {  //当访问时，发现未登录时的处理
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        String xrequestedWith = request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xrequestedWith)){    //判断请求是否为AJAX（异步）请求
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJsonString(403, "你还没有登录哦！"));
                            writer.close();
                        }else{//（同步请求）
                            //重定向到登录页面
                            System.out.println("没有登录");
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {    //当访问时，发现权限不够时的处理
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String xrequestedWith = request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xrequestedWith)){    //判断请求是否为AJAX（异步）请求
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJsonString(403, "你没有访问此功能的权限！"));
                            writer.close();
                        }else{//（同步请求）
                            //重定向到登录页面
                            System.out.println("没有权限");
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });

        //Security底层默认会拦截/logout请求，进行退出处理
        //覆盖它默认的逻辑，才能执行我们自己的退出代码
        http.logout().logoutUrl("/securitylogout");
    }


















}
