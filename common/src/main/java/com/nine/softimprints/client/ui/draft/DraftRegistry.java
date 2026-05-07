package com.nine.softimprints.client.ui.draft;

import java.util.*;
import java.util.function.Consumer;

public class DraftRegistry<K> {
	
	private final Map<K, List<DraftHolder<?>>> map = new LinkedHashMap<>();
	
	public DraftRegistry(){
	
	}
	
	public void addDraft(K key, DraftHolder<?> draft){
		map.computeIfAbsent(key, v -> new ArrayList<>()).add(draft);
	}
	
	public void actionFor(K key, Consumer<DraftHolder<?>> consumer){
		map.getOrDefault(key, List.of()).forEach(consumer);
	}
	
	public void actionForAll(Consumer<DraftHolder<?>> consumer){
		map.values().stream().flatMap(List::stream).forEach(consumer);
	}
	
}
