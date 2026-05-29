package com.nine.softimprints.ui.draft;

import java.util.function.UnaryOperator;

public interface DraftHolder<T> {

    T getCurrent();
    T getDraft();
	T getDefaultValue();

	boolean setDraft(T value);
	boolean updateDraft(UnaryOperator<T> updater);

	void discardDraft();
	void restoreDefaultDraft();
	void applyDraft();

    boolean hasUnsavedChanges();
    boolean isOverriddenFromDefault();

	void addListener(Runnable listener);
	void removeListener(Runnable listener);

}
