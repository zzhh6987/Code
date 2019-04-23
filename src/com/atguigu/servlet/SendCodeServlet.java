package com.atguigu.servlet;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atguigu.utils.CodeConfig;

import redis.clients.jedis.Jedis;

/**
 * 发送验证码的Servlet
 */
public class SendCodeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 获取用户输入的手机号
		String phoneNo = request.getParameter("phone_no");
		// 验空
		if (phoneNo == "") {
			System.out.println("ddddddd");
			return;
		}
		// 创建Jedis对象
		Jedis jedis = new Jedis(CodeConfig.HOST, CodeConfig.PORT);
		// 判断当前手机号今日发生验证码的次数
		// 拼接向Redis中获取计数器的key
		String countKey = CodeConfig.PHONE_PREFIX + phoneNo + CodeConfig.COUNT_SUFFIX;
		// 根据计数器的key从Redis中获取计数器的值
	    String count = jedis.get(countKey);
	    //判断count的值
	    if(count == null) {
	    	//证明当日该手机号还没有发送过验证码，此时需要向Redis中设置计数器的值为1，并设置有效时间为一天
	    	jedis.setex(countKey, CodeConfig.SECONDS_PER_DAY, "1");
	    }else if("3".equals(count)) {
	    	//当日已经发送过3次，给浏览器响应一个字符串limit
	    	response.getWriter().write("limit");
	    	//关闭Jedis
	    	jedis.close();
	    	return;
	    }else {
	    	//将Redis中的计数器的值加1
	    	jedis.incr(countKey);
	    }
		// 拼接向Redis中保存验证码的key
		String codeKey = CodeConfig.PHONE_PREFIX + phoneNo + CodeConfig.PHONE_SUFFIX;
		// 生成6位验证码
		String code = getCode(6);
		// 将验证码保存到Redis中，并设置它的有效时间是120秒
		jedis.setex(codeKey, CodeConfig.CODE_TIMEOUT, code);
		// 向手机号发送验证码，向控制台打印验证码
		System.out.println(code);
		// 给浏览器响应一个字符串true
		response.getWriter().write("true");
		// 关闭Jedis
		jedis.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	// 随机生成验证码的方法
	private String getCode(int len) {
		String code = "";
		for (int i = 0; i < len; i++) {
			int rand = new Random().nextInt(10);
			code += rand;
		}
		return code;
	}

}
