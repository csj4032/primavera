package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.UserConnection;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectKey;

@Mapper
public interface UserConnectionMapper {

	String INSERT_SQL = """
						INSERT 
							INTO USER_CONNECTION (EMAIL, PROVIDER, PROVIDER_ID, DISPLAY_NAME, PROFILE_URL, IMAGE_URL, ACCESS_TOKEN, EXPIRE_TIME)
						VALUES 
							(#{email}, #{provider, typeHandler=ProviderTypeHandler}, #{providerId}, #{displayName}, #{profileUrl}, #{imageUrl}, #{accessToken}, #{expireTime})
			""";

	@Insert(value = INSERT_SQL)
	@Options(useGeneratedKeys = true, keyProperty = "id")
	@SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", before = false, resultType = long.class)
	int save(UserConnection userConnection);
}