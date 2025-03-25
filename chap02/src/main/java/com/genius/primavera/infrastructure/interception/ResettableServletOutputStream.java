package com.genius.primavera.infrastructure.interception;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.OutputStream;

public class ResettableServletOutputStream extends ServletOutputStream {

	@Autowired
	protected com.genius.primavera.infrastructure.interception.PrimaveraInterceptor primaveraInterceptor;

	public OutputStream outputStream;
	private ResettableStreamHttpServletResponse wrappedResponse;
	private ServletOutputStream servletOutputStream = new ServletOutputStream() {
		boolean isFinished = false;
		boolean isReady = true;
		WriteListener writeListener = null;

		@Override
		public void setWriteListener(WriteListener writeListener) {
			this.writeListener = writeListener;
		}

		public boolean isReady() {
			return isReady;
		}

		@Override
		public void write(int w) throws IOException {
			outputStream.write(w);
			wrappedResponse.rawData.add(new Integer(w).byteValue());
		}
	};

	public ResettableServletOutputStream(ResettableStreamHttpServletResponse wrappedResponse) throws IOException {
		this.outputStream = wrappedResponse.response.getOutputStream();
		this.wrappedResponse = wrappedResponse;
	}

	@Override
	public void close() throws IOException {
		System.out.println("** RESPONSE CLOSE **");
		outputStream.close();
		primaveraInterceptor.writeResponsePayloadAudit(wrappedResponse);
	}

	@Override
	public void setWriteListener(WriteListener writeListener) {
		servletOutputStream.setWriteListener(writeListener);
	}

	@Override
	public boolean isReady() {
		return servletOutputStream.isReady();
	}

	@Override
	public void write(int w) throws IOException {
		servletOutputStream.write(w);
	}
}