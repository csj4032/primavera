package com.genius.primavera.infrastructure.interception;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.IOException;
import java.io.InputStream;

public class ResettableServletInputStream extends ServletInputStream {
	public InputStream inputStream;
	private ServletInputStream servletInputStream = new ServletInputStream() {
		boolean isFinished = false;
		boolean isReady = true;
		ReadListener readListener = null;

		public int read() throws IOException {
			int i = inputStream.read();
			isFinished = i == -1;
			isReady = !isFinished;
			return i;
		}

		@Override
		public boolean isFinished() {
			return isFinished;
		}

		@Override
		public boolean isReady() {
			return isReady;
		}

		@Override
		public void setReadListener(ReadListener readListener) {
			this.readListener = readListener;
		}
	};

	public int available() throws IOException {
		return inputStream.available();
	}

	public void close() throws IOException {
		inputStream.close();
	}

	public void mark(int readLimit) {
		inputStream.mark(readLimit);
	}

	public boolean markSupported() {
		return inputStream.markSupported();
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return inputStream.read(b, off, len);
	}

	public int readline(byte[] b, int off, int len) throws IOException {
		if (len <= 0) {
			return 0;
		}

		int count = 0, c;

		while ((c = read()) != -1) {
			b[off++] = (byte) c;
			count++;
			if (c == '\n' || count == len) {
				break;
			}
		}

		return count > 0 ? count : -1;
	}

	public void reset() throws IOException {
		inputStream.reset();
	}

	public long skip(long n) throws IOException {
		return inputStream.skip(n);
	}

	@Override
	public int read() throws IOException {
		return inputStream.read();
	}

	@Override
	public void setReadListener(ReadListener readListener) {
		servletInputStream.setReadListener(readListener);
	}

	public boolean isReady() {
		return servletInputStream.isReady();
	}

	public boolean isFinished() {
		return servletInputStream.isFinished();
	}
}
