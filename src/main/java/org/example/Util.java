package org.example;

import java.util.*;

public class Util {
    public static void main(String[] args) {
        Integer[] toHitCards = {2, 3, 7, 1};
        Integer[] myCards = {1, 4, 5, 6};

        Map<Integer, List<Integer>> map = new HashMap<>();
        for (Integer hc : toHitCards) {
            List<Integer> bcs = new LinkedList<>();
            for (Integer mc : myCards) {
                if (canHit(mc, hc)) {
                    bcs.add(mc);
                }
            }

            map.put(hc, bcs);
        }

        List<Integer[]> in = new LinkedList<>();
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            List<Integer> value = entry.getValue();
            for (Integer ir : value) {
                in.add(new Integer[]{ir, entry.getKey()});
            }
        }

        List<List<Integer[]>> result = new LinkedList<>();
        for (int i = toHitCards.length; i < in.size(); i++) {
            List<List<Integer[]>> combinations = combinations(in, i);

            for (List<Integer[]> combination: combinations){
                boolean passed = true;
                Set<Integer> unique = new HashSet<>();
                for (Integer[] integers : combination){
                    for (Integer integer: integers) {
                        if (unique.contains(integer)) {
                            passed = false;
                            break;
                        } else {
                            unique.add(integer);
                        }
                    }
                    if (!passed) {
                        break;
                    }
                }
                if (passed) {
                    result.add(combination);
                }
            }
        }

        for (List<Integer[]> arrList : result) {
            StringBuilder sb = new StringBuilder("[");
            for (int j = 0; j < arrList.size(); j++) {
                Integer[] arr = arrList.get(j);
                sb.append('[')
                        .append(arr[0])
                        .append(", ")
                        .append(arr[1]);
                if (j == arrList.size() - 1) {
                    sb.append(']');
                } else {
                    sb.append("], ");
                }
            }
            sb.append("], ");
            System.out.println(sb);
        }
    }

    private static boolean canHit(Integer i1, Integer i2) {
        return i1 > i2;
    }

    public static <T> List<List<T>> combinations(List<T> values, int size) {

        if (0 == size) {
            return Collections.singletonList(Collections.<T>emptyList());
        }

        if (values.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<T>> combination = new LinkedList<List<T>>();

        T actual = values.iterator().next();

        List<T> subSet = new LinkedList<T>(values);
        subSet.remove(actual);

        List<List<T>> subSetCombination = combinations(subSet, size - 1);

        for (List<T> set : subSetCombination) {
            List<T> newSet = new LinkedList<T>(set);
            newSet.add(0, actual);
            combination.add(newSet);
        }

        combination.addAll(combinations(subSet, size));

        return combination;
    }
}
