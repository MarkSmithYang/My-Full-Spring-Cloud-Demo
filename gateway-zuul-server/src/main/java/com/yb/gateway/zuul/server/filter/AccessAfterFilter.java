package com.yb.gateway.zuul.server.filter;

import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

/**
 * 在过滤之后处理(fastdfs等文件的跳转)
 */
@Component
public class AccessAfterFilter extends ZuulFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        String url = request.getRequestURI();
        if (url.startsWith("/group1/")) {
            String filename = request.getParameter("attname");
            if (StringUtils.isNotBlank(filename)) {
                try {
                    String codedFilename = filename;
                    HttpServletResponse response = requestContext.getResponse();
                    String agent = request.getHeader("USER-AGENT");
                    if (null != agent && -1 != agent.indexOf("MSIE") || null != agent
                            && -1 != agent.indexOf("Trident") || null != agent && -1 != agent.indexOf("Edge")) {// ie浏览器及Edge浏览器
                        String name = java.net.URLEncoder.encode(filename, "UTF-8");
                        codedFilename = name;
                    } else if (null != agent && -1 != agent.indexOf("Mozilla")) {// 火狐,Chrome等浏览器
                        codedFilename = new String(filename.getBytes("UTF-8"), "ISO-8859-1");
                    }
                    response.setHeader("Content-Disposition", "attachment;fileName="+ codedFilename);

                } catch (UnsupportedEncodingException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        return null;
    }

}
