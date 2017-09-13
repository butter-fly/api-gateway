package com.using.api.core;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ApiGatewayServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	ApplicationContext context;
	private ApiGatewayHand apiGatewayHand;

	public void init() throws ServletException {
		super.init();
		context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		apiGatewayHand = context.getBean(ApiGatewayHand.class);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		apiGatewayHand.handle(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		apiGatewayHand.handle(request, response);
	}

}
