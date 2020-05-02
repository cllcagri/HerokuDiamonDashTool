package io.growbymastery.ppmtool.security;

import io.growbymastery.ppmtool.domain.User;
import io.growbymastery.ppmtool.payload.JWTTokenProvider;
import io.growbymastery.ppmtool.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;

import static io.growbymastery.ppmtool.security.SecurityConstants.HEADER_STRING;
import static io.growbymastery.ppmtool.security.SecurityConstants.TOKEN_PREFIX;

@Component
public class JwtAuthenticationFilter extends GenericFilterBean {

  @Autowired private JWTTokenProvider jwtTokenProvider;

  @Autowired private CustomUserDetailsService customUserDetailsService;

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    try {

      if(jwtTokenProvider == null){
        ServletContext servletContext = servletRequest.getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        assert webApplicationContext != null;
        jwtTokenProvider = webApplicationContext.getBean(JWTTokenProvider.class);
      }

      if(customUserDetailsService == null){
        ServletContext servletContext = servletRequest.getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        assert webApplicationContext != null;
        customUserDetailsService = webApplicationContext.getBean(CustomUserDetailsService.class);
      }

      String jwt = getJwtFromRequest((HttpServletRequest) servletRequest);

      if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
        Long userId = jwtTokenProvider.getUserIdFromJWT(jwt);
        User userDetails = customUserDetailsService.loadUserById(userId);

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList());

        authenticationToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails((HttpServletRequest) servletRequest));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      }

    } catch (Exception e) {
      logger.error("Could not set user authentication in security context", e);
    }

    filterChain.doFilter(servletRequest, servletResponse);
  }

  private String getJwtFromRequest(HttpServletRequest httpServletRequest) {
    String bearerToken = httpServletRequest.getHeader(HEADER_STRING);

    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
      return bearerToken.substring(7, bearerToken.length());
    }

    return null;
  }
}
