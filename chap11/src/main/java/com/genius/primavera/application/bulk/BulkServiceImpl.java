package com.genius.primavera.application.bulk;

import com.genius.primavera.domain.mapper.BulkMapper;
import com.genius.primavera.domain.model.Child;
import com.genius.primavera.domain.model.Parent;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BulkServiceImpl implements BulkService {

	private final BulkMapper bulkMapper;

	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public int bulkInsert() {
		Parent parent = getParent();
		parentInsert(parent);
		childrenInsert(parent.getId(), getChildren());
		return 0;
	}

	@NotNull
	private List<Child> getChildren() {
		List<Child> children = new ArrayList();
		for (int i = 1; i <= 10; i++) {
			Child child = Child.builder().age(10).name("child" + i).build();
			children.add(child);
		}
		return children;
	}

	private Parent getParent() {
		return Parent.builder().age(100).name("parent").build();
	}

	private void childrenInsert(long parentId, List<Child> children) {
		bulkMapper.childrenBulkInsert(parentId, children);
	}

	private void parentInsert(Parent parent) {
		bulkMapper.parentInsert(parent);
	}
}
