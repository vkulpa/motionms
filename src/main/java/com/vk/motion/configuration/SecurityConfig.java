package com.vk.motion.configuration;

import com.vk.motion.configuration.ApplicationConfig.HmacEncoder;
import com.vk.motion.service.ViberService;
import com.vk.motion.utils.WebUtils;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.GenericFilterBean;

@Configuration
@EnableWebSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final HmacEncoder encoder;
  private final ViberService viberService;

  private SignatureAuthenticationService signatureAuthenticationService =
      new SignatureAuthenticationService();

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .mvcMatchers("/health", "/favicon.ico", "/error", "/bot")
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .addFilterBefore(new SignatureAuthFilter(), BasicAuthenticationFilter.class)
        .csrf()
        .disable()
        .exceptionHandling()
        .authenticationEntryPoint(new AppAuthenticationEntryPoint());
  }

  class AppAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException)
        throws IOException, ServletException {
      String accessDenied = WebUtils.getAccessDeniedText(request);
      viberService.sendTextWithKeyboard(accessDenied);
      log.error(accessDenied);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  class SignatureAuthFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
        throws IOException, ServletException {
      Authentication authentication =
          signatureAuthenticationService.getAuthentication((HttpServletRequest) request);

      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);
    }
  }

  class SignatureAuthenticationService {

    Authentication getAuthentication(HttpServletRequest request) {
      String signature = request.getParameter("signature");
      if (!ObjectUtils.isEmpty(signature)
          && !request.getDispatcherType().equals(DispatcherType.ERROR)) {
        String expected = encoder.encode(request.getRequestURI());
        if (signature.equals(expected)) {
          return new UsernamePasswordAuthenticationToken(null, null, Collections.emptyList());
        } else {
          String wrongSignature = WebUtils.getWrongSignText(request, signature, expected);
          viberService.sendTextWithKeyboard(wrongSignature);
          log.error(wrongSignature);
        }
      }
      return null;
    }
  }
}
