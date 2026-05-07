package com.nine.softimprints.client.ui.draft;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class SimpleDraft<T> implements DraftHolder<T> {
	
	private T current;
	private T draft;
	private final T defaultValue;
	protected final UnaryOperator<T> copier;
	private final List<Runnable> listeners = new ArrayList<>();
	
	public SimpleDraft(T current, T defaultValue, UnaryOperator<T> copier) {
		this.copier = Objects.requireNonNull(copier, "copier");
		this.current = copy(current);
		this.draft = copy(current);
		this.defaultValue = copy(defaultValue);
	}
	
	@Override
	public T getCurrent() {
		return current;
	}
	
	@Override
	public T getDraft() {
		return draft;
	}
	
	@Override
	public T getDefaultValue() {
		return defaultValue;
	}
	
	@Override
	public boolean setDraft(T value) {
		if (same(draft, value)) return false;
		draft = copy(value);
		notifyListeners();
		return true;
	}

	@Override
	public boolean updateDraft(UnaryOperator<T> updater) {
		Objects.requireNonNull(updater, "updater");
		return setDraft(updater.apply(draft));
	}

	@Override
	public void applyDraft() {
		if (same(current, draft)) return;
		current = copy(draft);
		notifyListeners();
	}

	@Override
	public void discardDraft() {
		if (same(draft, current)) return;
		draft = copy(current);
		notifyListeners();
	}

	
	@Override
	public void restoreDefaultDraft() {
		if (same(draft, defaultValue)) return;
		draft = copy(defaultValue);
		notifyListeners();
	}

	@Override
	public boolean hasUnsavedChanges() {
		return !Objects.equals(draft, current);
	}
	
	@Override
	public boolean isOverriddenFromDefault() {
		return !Objects.equals(draft, defaultValue);
	}
	
	@Override
	public void addListener(Runnable listener) {
		listeners.add(Objects.requireNonNull(listener, "listener"));
	}
	
	@Override
	public void removeListener(Runnable listener) {
		listeners.remove(listener);
	}
	
	protected void notifyListeners() {
		new ArrayList<>(listeners).forEach(Runnable::run);
	}

	protected boolean same(T v1, T v2) {
		return (Objects.equals(v1, v2));
	}

	protected T copy(T value) {
		return copier.apply(value);
	}
	
}
