package com.example.rest_api;

import java.util.ArrayList;

public interface Subject {

	public ArrayList<Observer<? extends Subject>> observers = new ArrayList<Observer<? extends Subject>>();

	default void attach(Observer<? extends Subject> o) {
		observers.add(o);
	};

	default void detach(Observer<? extends Subject> o) {
		observers.add(o);
	};

	@SuppressWarnings("unchecked")
	default void notifyObservers(ObserverEvents event) {
		for (Observer<? extends Subject> o : observers) {
			((Observer<Subject>) o).update(event, this);
		}
	};
}
