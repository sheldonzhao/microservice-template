package com.sheldon.zuulserver.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author Sheldon Zhao
 * @date 2019-11-18
 */

@Component
public class RequestLogFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(RequestLogFilter.class);

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
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        String requestBody = "";
        String responseBody = "";
        //get parameters
        Map<String, String[]> paramsMap = request.getParameterMap();
        StringBuffer paramStr = new StringBuffer();
        if (paramsMap != null) {
            for (Map.Entry<String, String[]> entry : paramsMap.entrySet()) {
                paramStr.append("[").append(entry.getKey()).append("=").append(printStringArray(entry.getValue())).append("]");
            }
        }
        try {
            //get request body
            InputStream in = request.getInputStream();
            requestBody = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
            requestBody = requestBody.replace('\n', ' ');
            // get response
            InputStream out = ctx.getResponseDataStream();
            responseBody = StreamUtils.copyToString(out, Charset.forName("UTF-8"));
            ctx.setResponseBody(responseBody);
            responseBody = responseBody.replace('\n', ' ');
        } catch (Exception e) {
            e.printStackTrace();
        }

        //get duration time
        long startTime = (long) ctx.get("startTime");
        long duration = System.currentTimeMillis() - startTime;
        // print log
        StringBuffer requestInfo = new StringBuffer();
        requestInfo.append(String.format("%s >>> [%s] ", request.getMethod(), request.getRequestURL().toString()));
        requestInfo.append(String.format("%s >>> [%s] ", "requestParams", paramStr));
        requestInfo.append(String.format("%s >>> [%s] ", "requestBody", requestBody));
        requestInfo.append(String.format("%s >>> [%s] ", "responseBody", responseBody));
        requestInfo.append(String.format("%s >>> [%s]", "duration", String.valueOf(duration)));
        log.info(requestInfo.toString());
        //System.out.println(requestInfo);
        return null;
    }

    private String printStringArray(String[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

}
