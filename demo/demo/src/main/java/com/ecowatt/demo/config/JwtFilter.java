package com.ecowatt.demo.config;

import com.ecowatt.demo.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFilter(
            JwtService jwtService
    ){
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String auth =
                request.getHeader("Authorization");

        if(auth != null &&
                auth.startsWith("Bearer ")){

            String token =
                    auth.substring(7);

            try{

                String email =
                        jwtService.validarToken(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of()
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);

            }catch(Exception e){

                response.setStatus(401);
                return;
            }
        }

        chain.doFilter(request,response);
    }
}