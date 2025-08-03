package com.genius.primavera.testcontainer.v2.parallel;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * 병렬 실행 테스트 통합 스위트
 * 
 * 실행 방법:
 * - IDE에서: junit.jupiter.execution.parallel.enabled=true 설정 후 실행
 * - Gradle에서: test { systemProperty 'junit.jupiter.execution.parallel.enabled', 'true' }
 * 
 * 병렬 실행 설정을 확인하기 위한 junit-platform.properties 파일 생성:
 * junit.jupiter.execution.parallel.enabled=true
 * junit.jupiter.execution.parallel.mode.default=concurrent
 * junit.jupiter.execution.parallel.mode.classes.default=concurrent
 */
@Suite
@SelectClasses({
    ParallelExecutionTestA.class,
    ParallelExecutionTestB.class,
    ParallelExecutionTestC.class,
    ParallelResourceSharingTest.class
})
@SuiteDisplayName("병렬 실행 통합 테스트 스위트")
class ParallelExecutionSuite {
    // JUnit 5 Suite는 단순히 테스트를 그룹화하는 용도
    // @BeforeAll, @AfterAll 등은 사용할 수 없음
    // 각 테스트 클래스에서 독립적으로 처리해야 함
}