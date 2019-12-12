package com.genius.primavera;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Params {
	List<EnumType> enumTypes;
	List<String> names;
	List<Integer> ages;
}
