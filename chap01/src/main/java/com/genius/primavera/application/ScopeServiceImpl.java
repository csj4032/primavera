package com.genius.primavera.application;

import com.genius.primavera.domain.User;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Collections;
import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Service("scopeService")
public class ScopeServiceImpl implements ScopeService {

	@Override
	public List<User> getUsers() {
		return Collections.EMPTY_LIST;
	}
}