package com.ld.Filter;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：AuthFilter   
* 类描述： 登录及访问权限控制
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:29:47   
* @version        
*/
public class AuthFilter implements Filter { 

    protected FilterConfig filterConfig;
    
    public void destroy() { 
             
    } 
    
    /**
     * 登录验证，控制用户访问权限
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException { 
        // 获取URI地址 
         HttpServletRequest req = (HttpServletRequest) request;
         HttpServletResponse res = (HttpServletResponse) response;
         HttpSession session = req.getSession();
         String uri = req.getRequestURI(); 
         String ctx=req.getContextPath(); 
         uri = uri.substring(ctx.length()); 
      
         if (req.getSession()== null) { 
        	 if (req.getHeader("x-requested-with") != null && req.getHeader("x-requested-with").equals("XMLHttpRequest")) { // ajax请求 
        		 res.setHeader("sessionstatus", "timeout"); 
        	 } else { 
        		 res.sendRedirect("/index.jsp"); 
        		 return; 
        	 }
         }
         // 除了来自登录页面否则进行session验证
         if (!uri.contains("/login.jsp")) {
           if (session.getAttribute("user") == null) {
             res.sendRedirect(req.getContextPath() + "/login.jsp");
             return;
           }
           else{
        	   int add=(int)session.getAttribute("add");
        	   int test=(int)session.getAttribute("test");
        	   int modify=(int)session.getAttribute("modify");
	         
        	   //验证用户是否具有访问添加模板页面的权限
        	   if(uri.contains("regexTemplate")||uri.contains("QuestionTemplate")) { 
        		   if (add==0) {
        			   request.setAttribute("message","您没有权限访问该页面"); 
		               request.getRequestDispatcher("/index.jsp").forward(req,res); 
		               return ; 
		            }
        		   else
        			   chain.doFilter(request,response);
	           }
        	   //验证用户是否具有访问编辑模板页面的权限
        	   if(uri.contains("modify")) {
        		   if (modify==0) { 
        			   request.setAttribute("message","您没有权限访问该页面"); 
		               request.getRequestDispatcher("/index.jsp").forward(req,res); 
		               return; 
		           }
        		   else
        			   chain.doFilter(request,response);
		       } 
        	 //验证用户是否具有访问测试页面的权限
        	   if(uri.contains("test")) { 
        		   if (test==0) { 
        			   request.setAttribute("message","您没有这个权限"); 
		               request.getRequestDispatcher("/index.jsp").forward(req,res); 
		               return; 
		           }
        		   else
        			   chain.doFilter(request,response);
		       }
        	   if(uri.contains("index")||uri.contains("Update")||uri.equals("/")) {
        		   chain.doFilter(request,response);
        	   }
        	   
        	   }
         }
         else chain.doFilter(request,response);
         
    }

    public void init(FilterConfig arg0) throws ServletException { 
             
    } 
    
}