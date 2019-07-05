package me.sise.batch.application.service;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Rank<T extends Number> {
    private final int ranking;
    private final T value;

    public Rank(int ranking, T value) {
        this.ranking = ranking;
        this.value = value;
    }

    public static <V extends Number, U> Map<String, Rank<V>> aggregateByRank(Map<String, List<U>> data,
                                                                             Function<List<U>, V> function) {
        List<ImmutablePair<String, V>> aggregateList = new ArrayList<>();

        data.forEach((key, datas) -> {
            V aggregateValue = function.apply(datas);
            aggregateList.add(new ImmutablePair<>(key, aggregateValue));
        });

        aggregateList.sort((p1, p2) -> (int) (p2.getValue().doubleValue() - p1.getValue().doubleValue()));

        Map<String, Rank<V>> aggregateMap = new HashMap<>();
        for (int i = 0; i < aggregateList.size(); ++i) {
            int ranking = i + 1;
            V value = aggregateList.get(i).getValue();
            String key = aggregateList.get(i).getKey();
            aggregateMap.put(key, new Rank<>(ranking, value));
        }
        return aggregateMap;
    }

    public int getRanking() {
        return ranking;
    }

    public T getValue() {
        return value;
    }

}
