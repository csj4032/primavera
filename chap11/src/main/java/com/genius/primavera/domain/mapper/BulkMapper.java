package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Child;
import com.genius.primavera.domain.model.Parent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BulkMapper {

	int parentInsert(Parent parent);

	int childrenBulkInsert(@Param("parentId") long parentId, @Param("list") List<Child> children);
}
