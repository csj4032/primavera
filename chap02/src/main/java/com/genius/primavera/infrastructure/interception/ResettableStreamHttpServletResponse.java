package com.genius.primavera.infrastructure.interception;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ResettableStreamHttpServletResponse extends HttpServletResponseWrapper {

	public List<Byte> rawData = new ArrayList<>();
	public HttpServletResponse response;
	private ResettableServletOutputStream servletStream;

	ResettableStreamHttpServletResponse(HttpServletResponse response) throws IOException {
		super(response);
		this.response = response;
		this.servletStream = new ResettableServletOutputStream(this);
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return servletStream;
	}

	public PrintWriter getWriter() throws IOException {
		String encoding = getCharacterEncoding();
		if (encoding != null) {
			return new PrintWriter(new OutputStreamWriter(servletStream, encoding));
		} else {
			return new PrintWriter(new OutputStreamWriter(servletStream));
		}
	}
}
