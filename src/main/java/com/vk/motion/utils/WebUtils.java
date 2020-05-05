package com.vk.motion.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WebUtils {

  public static String getAccessDeniedText(HttpServletRequest request) {
    return "Access denied "
        + request.getMethod()
        + " request URI "
        + getFullURI(request)
        + " With headers: "
        + getHeaders(request)
        + getRequestBody(request);
  }

  public static String getWrongSignText(
      HttpServletRequest request, String currentSign, String expectedSign) {
    return "Wrong signature "
        + currentSign
        + " ( Expected "
        + expectedSign
        + " ), for "
        + request.getMethod()
        + " request URI "
        + getFullURI(request)
        + " With headers: "
        + getHeaders(request)
        + getRequestBody(request);
  }

  private static String getRequestBody(HttpServletRequest request) {
    if (!request.getMethod().equalsIgnoreCase("GET")) {
      // formData goes here
      if (request.getParameterMap().size() > 0) {
        return " " + new String(Base64.getEncoder().encode(getParams(request).getBytes()));
      } else {
        // JSON && fileUpload go here
        // FileUpload doesn't work yet
        try (BufferedReader br =
            new BufferedReader(new InputStreamReader(request.getInputStream()))) {
          ByteBuffer body = ByteBuffer.allocate(request.getContentLength());
          int data = br.read();
          while (data != -1) {
            body.put((byte) data);
            data = br.read();
          }
          body.rewind();
          return " " + new String(Base64.getEncoder().encode(body).array());
        } catch (Exception e) {
          return e.getMessage();
        }
      }
    }

    return "";
  }

  private static String getParams(HttpServletRequest request) {
    Map<String, String> params = new HashMap<>();

    Enumeration parameterNames = request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String key = (String) parameterNames.nextElement();
      String value = StringUtils.arrayToDelimitedString(request.getParameterValues(key), ",");
      params.put(key, value);
    }

    return params.toString();
  }

  private static String getHeaders(HttpServletRequest request) {
    Map<String, String> map = new HashMap<>();

    Enumeration headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String key = (String) headerNames.nextElement();
      String value = request.getHeader(key);
      map.put(key, value);
    }

    return map.toString();
  }

  private static String getFullURI(HttpServletRequest request) {
    return request.getRequestURI()
        + Optional.ofNullable(request.getQueryString()).map("?"::concat).orElse("");
  }
}
