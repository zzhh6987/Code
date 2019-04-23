package com.atguigu.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atguigu.utils.CodeConfig;

import redis.clients.jedis.Jedis;

/**
 * 校验验证码的Servlet
 */
public class CheckCodeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 获取手机号和用户输入的验证码
		String phoneNo = request.getParameter("phone_no");
		String inputCode = request.getParameter("verify_code");
		// 验空
		if (phoneNo == "" || inputCode == "") {
			return;
		}
		// 拼接向Redis中获取验证码的key
		String codeKey = CodeConfig.PHONE_PREFIX + phoneNo + CodeConfig.PHONE_SUFFIX;
		// 创建Jedis对象
		Jedis jedis = new Jedis(CodeConfig.HOST, CodeConfig.PORT);
		//从Redis中获取验证码
		String redisCode = jedis.get(codeKey);
		//判断用户输入的验证码与从Redis中获取的验证码是否一致
		if(inputCode.equals(redisCode)) {
			//将Redis中的验证码移除
			jedis.del(codeKey);
			//验证码正确，给浏览器响应一个字符串true
			response.getWriter().write("true");
		}
		//关闭jedis
		jedis.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
