package com.zipdb.core.datatype;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Represents a Redis-like sorted set (ZSet).
 * Maintains elements with associated scores in sorted order.
 */
public class SortedSetType implements DataType {

    private final ConcurrentHashMap<String, Double> memberScoreMap; // member -> score
    private final ConcurrentSkipListMap<Double, Set<String>> scoreMemberMap; // score -> members

    public SortedSetType() {
        this.memberScoreMap = new ConcurrentHashMap<>();
        this.scoreMemberMap = new ConcurrentSkipListMap<>();
    }

    @Override
    public String getType() {
        return "sortedset";
    }

    public void add(String member, double score) {
        Double oldScore = memberScoreMap.put(member, score);

        if (oldScore != null) {
            scoreMemberMap.get(oldScore).remove(member);
            if (scoreMemberMap.get(oldScore).isEmpty()) {
                scoreMemberMap.remove(oldScore);
            }
        }

        scoreMemberMap.computeIfAbsent(score, k -> new HashSet<>()).add(member);
    }

    public boolean remove(String member) {
        Double score = memberScoreMap.remove(member);
        if (score != null) {
            Set<String> members = scoreMemberMap.get(score);
            members.remove(member);
            if (members.isEmpty()) {
                scoreMemberMap.remove(score);
            }
            return true;
        }
        return false;
    }

    public Double getScore(String member) {
        return memberScoreMap.get(member);
    }

    public List<String> rangeByScore(double min, double max) {
        List<String> result = new ArrayList<>();
        scoreMemberMap.subMap(min, true, max, true).values()
                .forEach(result::addAll);
        return result;
    }

    public Map<String, Double> getAllMembers() {
        return memberScoreMap;  // returns the internal member-score map
    }
}
