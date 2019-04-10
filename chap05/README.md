## chap05
### Validation
* AutoConfigureWebTestClient 이용한 Controller 테스트 [참고](https://docs.spring.io/spring/docs/5.1.6.RELEASE/spring-framework-reference/testing.html#webtestclient-tests)
* Nickname 별도의 Validator, Annotation 생성
```java
public class NicknameValidator implements ConstraintValidator<Nickname, String> {

	@Override
	public void initialize(Nickname constraintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) return false;
		return value.matches("^[0-9a-zA-Z가-힣]{2,20}$");
	}
}
```

```java
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NicknameValidator.class)
public @interface Nickname {

	String message() default "{com.genius.primavera.validate.nickname.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
```

* 저장과 수정에 따른 필수값 그룹
```
public interface SaveGroup extends Default {
	}

	public interface UpdateGroup extends Default {
	}

	@Min(value = 1, groups = UpdateGroup.class)
	private long id;
```

* 객체 내부 속성 @Valid 재귀 검사

```
@Valid
	@NotNull
	@Size(min = 1)
	private List<Role> roles;
```