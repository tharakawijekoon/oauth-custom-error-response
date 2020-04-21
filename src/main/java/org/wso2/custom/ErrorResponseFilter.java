package org.wso2.custom;

import java.io.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;


public class ErrorResponseFilter implements Filter {
    private static final Log log = LogFactory.getLog(ErrorResponseFilter.class);
    private static String ERROR_DESCRIPTION = "error_description";
    private static String CUSTOM_ERROR = "custom_error_code";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        JsonResponseWrapper capturingResponseWrapper = new JsonResponseWrapper(
                (HttpServletResponse) response);

        chain.doFilter(request, capturingResponseWrapper);

        if (response.getContentType() != null
                && response.getContentType().contains("application/json")) {

            String content = capturingResponseWrapper.getCaptureAsString();

            //String test = "{     \"error_description\": \"Error when handling event : PRE_AUTHENTICATION# testing\",     \"error\": \"invalid_grant\" }";

            if (content.contains(ERROR_DESCRIPTION)){

                JSONParser parser = new JSONParser();
                try {
                    // modify content here
                    JSONObject json = (JSONObject) parser.parse(content);
                    String[] msgs = json.get(ERROR_DESCRIPTION).toString().split("#");

                    if(msgs.length==2){
                        json.replace(ERROR_DESCRIPTION, msgs[0].trim());
                        json.put(CUSTOM_ERROR, msgs[1].trim());
                        content = json.toJSONString();
                        if (log.isDebugEnabled()) {
                            log.debug(content);
                        }
                    }
                } catch (ParseException e){
                    log.error("Error while parsing error response in custom filter", e);
                }

            }
            response.getWriter().write(content);
        }
    }

    @Override
    public void destroy() {

    }

}
