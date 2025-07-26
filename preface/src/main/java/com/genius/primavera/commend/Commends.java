package com.genius.primavera.commend;

import com.genius.primavera.Person;

import java.util.List;

public interface Commends<T extends Person> {

	List<T> execute(List<T> entry);

}
