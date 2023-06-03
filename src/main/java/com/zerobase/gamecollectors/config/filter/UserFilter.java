package com.zerobase.gamecollectors.config.filter;

import com.zerobase.gamecollectors.common.TokenType;
import com.zerobase.gamecollectors.common.UserType;
import com.zerobase.gamecollectors.common.UserVo;
import com.zerobase.gamecollectors.config.JwtAuthenticationProvider;
import com.zerobase.gamecollectors.service.UserDetailService;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@WebFilter(urlPatterns = "/user/*")
@RequiredArgsConstructor
public class UserFilter implements Filter {

    private final JwtAuthenticationProvider provider;
    private final UserDetailService userDetailService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String token = req.getHeader("X-AUTH-TOKEN");
        if (provider.validateToken(token, UserType.USER).equals(TokenType.INVALID_TOKEN)) {
            throw new ServletException("Invalid Token");
        }
        UserVo vo = provider.getUserVo(token);
        userDetailService.findByIdAndEmail(vo.getId(), vo.getEmail())
            .orElseThrow(() -> new ServletException("Invalid Access."));

        chain.doFilter(request, response);

    }
}
