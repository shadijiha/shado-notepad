package com.observer;

import java.util.*;

public abstract class Observable<T> {
	protected List<Observer<T>> observers = new ArrayList<>();

	public void updateAll() {
		for (var o :
				observers) {
			o.update(getData());
		}
	}

	public abstract T getData();

	public void addObserver(Observer<T> o) {
		observers.add(o);
	}

	public void removeObserver(Observer<T> o) {
		observers.remove(o);
	}
}
