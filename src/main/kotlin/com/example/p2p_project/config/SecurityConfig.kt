package com.example.p2p_project.config

import com.example.p2p_project.handlers.AuthErrorHandler
import com.example.p2p_project.services.MyUserDetailsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Value("\${application.info.api}")
    private lateinit var apiLink: String
    @Bean
    fun userDetailsService(): UserDetailsService {
        return MyUserDetailsService()
    }


    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity.csrf{it.disable()}
            .authorizeHttpRequests {
                it.requestMatchers("${apiLink}/sign-in/**", "${apiLink}/sign-up/**").permitAll()
                it.requestMatchers("${apiLink}/account/**").authenticated()
                it.anyRequest().permitAll()
            }
            .formLogin{
                it.loginPage("${apiLink}/sign-in")
                it.failureHandler(AuthErrorHandler(apiLink))
                it.defaultSuccessUrl("${apiLink}/account/welcome")
            }
            .build()
    }

    @Bean
    fun authenticationProvider():AuthenticationProvider{
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(userDetailsService())
        provider.setPasswordEncoder(passwordEncoder())
        return provider
    }

    @Bean
    fun passwordEncoder():PasswordEncoder{
        return BCryptPasswordEncoder()
    }
}