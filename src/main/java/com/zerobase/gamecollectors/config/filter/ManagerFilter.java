package com.zerobase.gamecollectors.config.filter;

import com.zerobase.gamecollectors.common.UserVo;
import com.zerobase.gamecollectors.config.JwtAuthenticationProvider;
import com.zerobase.gamecollectors.common.TokenType;
import com.zerobase.gamecollectors.service.ManagerDetailService;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@WebFilter(urlPatterns = "/manager/*")
@RequiredArgsConstructor
public class ManagerFilter implements Filter {

    private final JwtAuthenticationProvider provider;
    private final ManagerDetailService managerDetailService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String token = req.getHeader("X-AUTH-TOKEN");
        if (provider.validateToken(token).equals(TokenType.INVALID_TOKEN)) {
            throw new ServletException("Invalid Token");
        }
        UserVo vo = provider.getUserVo(token);
        managerDetailService.findByIdAndEmail(vo.getId(), vo.getEmail())
            .orElseThrow(() -> new ServletException("Invalid Access."));

        chain.doFilter(request, response);

    }
}
