package cn.geoair.geoairteam.gwc.wcs.config;

import cn.hutool.core.collection.ListUtil;
import cn.geoair.geoairteam.gwc.service.config.GeoWebCacheConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 替代 geowebcache-security.xml 的完整 Spring Boot 配置
 * 修复用户状态、角色权限、异常处理等核心问题
 */
@Configuration
@EnableWebSecurity
public class GeoWebCacheSecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    GeoWebCacheConfig geoWebCacheConfig;

    // ===================== 1. 密码编码器配置 =====================
    @Bean
    public PasswordEncoder defaultPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public Map<String, PasswordEncoder> passwordEncodersMap() {
        Map<String, PasswordEncoder> encoderMap = new HashMap<>();
        encoderMap.put("noop", NoOpPasswordEncoder.getInstance());
        encoderMap.put("bcrypt", new BCryptPasswordEncoder());
        return encoderMap;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        DelegatingPasswordEncoder delegatingEncoder = new DelegatingPasswordEncoder(
                "bcrypt",
                passwordEncodersMap()
        );
        delegatingEncoder.setDefaultPasswordEncoderForMatches(defaultPasswordEncoder());
        return delegatingEncoder;
    }

    // ===================== 2. 用户认证配置 =====================
    @Bean
    public UserDetailsService userDetailsService() {
        String adminPassword = geoWebCacheConfig.getAdminPassword();
        String adminUserName = geoWebCacheConfig.getAdminUserName();
        return username -> {
            // 只允许admin用户登录，非admin抛出异常（符合规范）
            if (!adminUserName.equals(username)) {
                throw new UsernameNotFoundException("用户 " + username + " 不存在");
            }

            return new UserDetails() {
                @Override
                public Collection<? extends GrantedAuthority> getAuthorities() {
                    // 关键修复：添加ROLE_前缀，匹配hasRole的校验规则
                    return ListUtil.of(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"));
                }

                @Override
                public String getPassword() {
                    // 明文密码（因使用NoOpPasswordEncoder）
                    return adminPassword;
                }

                @Override
                public String getUsername() {
                    return adminUserName;
                }


                @Override
                public boolean isAccountNonExpired() {
                    return true;
                }

                @Override
                public boolean isAccountNonLocked() {
                    return true;
                }

                @Override
                public boolean isCredentialsNonExpired() {
                    return true;
                }

                @Override
                public boolean isEnabled() {
                    return true;
                }
            };
        };
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService())
                .passwordEncoder(passwordEncoder());
    }

    // ===================== 3. HTTP安全规则配置 =====================
    @Bean
    public BasicAuthenticationEntryPoint authenticationEntryPoint() {
        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("GeoWebCache Secured");
        return entryPoint;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests()
//                // ========== 核心修改：仅对指定路径鉴权，其余全部放行 ==========
//                // 1. 放行你原本指定的两个公开GET接口（可选，若不需要可删除）
//                .antMatchers(HttpMethod.GET, "/rest/web/**").permitAll()
//                .antMatchers(HttpMethod.GET, "/rest/wmts/WMTSCapabilities.xml").permitAll()
//                // 2. 仅对这两个路径要求ADMINISTRATOR角色鉴权
//                .antMatchers("/rest/**").hasRole("ADMINISTRATOR")
//                .antMatchers("/geoair/demo").hasRole("ADMINISTRATOR")
//                // 3. 关键：所有其他路径直接放行（替代原来的anyRequest().authenticated()）
//                .anyRequest().permitAll()
//                .and()
//                // 启用Basic认证（仅在访问需要鉴权的路径时触发）
//                .httpBasic()
//                .authenticationEntryPoint(authenticationEntryPoint())
//                .and()
//                // 禁用CSRF（适合非浏览器客户端访问）
//                .csrf().disable()
//                // 可选：关闭session（Basic认证无需session）
//                .sessionManagement().disable();
        http
                .authorizeRequests()
                // ========== 只对根路径 / 进行鉴权 ==========
                .antMatchers("/").hasRole("ADMINISTRATOR")
                .antMatchers("/home").hasRole("ADMINISTRATOR")
                .antMatchers("/demo").hasRole("ADMINISTRATOR")
                .antMatchers("/geoair/demo").hasRole("ADMINISTRATOR")
                .antMatchers("/geoair/demov2").hasRole("ADMINISTRATOR")
                .antMatchers("/geoair/reloadLayerGroup").hasRole("ADMINISTRATOR")
                .antMatchers("/geoair/PreviewXML").hasRole("ADMINISTRATOR")
                // 所有其他路径全部放行
                .anyRequest().permitAll()
                .and()
                // 启用Basic认证（仅在访问需要鉴权的路径时触发）
                .httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint())
                .and()
                // 禁用CSRF（适合非浏览器客户端访问）
                .csrf().disable()
                // 可选：关闭session（Basic认证无需session）
                .sessionManagement().disable();
    }
}
