import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;


public class Main {
    private static final long seed = 123L;

    public static void main(String[] args) {
        Random random = new Random(seed);
        long startTime, endTime;

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the array size: ");
        int arraySize = scanner.nextInt();
        //int arraySize = 10;

        System.out.print("Enter the thread count: ");
        int threadCount = scanner.nextInt();
        //int threadCount = 4;

        scanner.close();

        int[] shuffledArray = generateAndShuffleArray(arraySize, random);



        //System.out.println("Initial Array:");
        //printShuffledArr(shuffledArray);

        List<Interval> intervals = generate_intervals(0, arraySize - 1);
        //printIntervals(intervals);

        Map<String,Boolean> intervalMap = new HashMap<>();

        // complete the intervalMap with booleans
        for(Interval interval : intervals){
            intervalMap.put(interval.toStringInterval(),false);
        }

        //for(Interval interval : intervals){
        //    System.out.println(interval.toStringInterval() + " : " + intervalMap.get(interval.toStringInterval()));
        //}

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        startTime = System.currentTimeMillis();
        int idx = 0;
        while(!intervalMap.values().stream().allMatch(value -> value)){
            if(idx < intervals.size()){
                Interval interval = intervals.get(idx);

                //if leafnode
                if(interval.getStart() == interval.getEnd()){
                    executor.submit(() -> merge(shuffledArray, interval.getStart(), interval.getEnd()));
                    intervalMap.put(interval.toStringInterval(),true);
                }else{
                    //System.out.println("PARENT INTERVAL: " + interval.toStringInterval());
                    List<Interval> subIntervals = generate_intervals(interval.getStart(), interval.getEnd());
                    subIntervals.remove(subIntervals.size() - 1);

                    List<String> keysToCheck = new ArrayList<>();
                    for(Interval subInterval : subIntervals){
                        keysToCheck.add(subInterval.toStringInterval());
                    }

                    boolean allTrueCheck = keysToCheck.stream()
                            .allMatch(key -> intervalMap.getOrDefault(key, false));

                    if(allTrueCheck){
                        executor.submit(() -> merge(shuffledArray, interval.getStart(), interval.getEnd()));
                        intervalMap.put(interval.toStringInterval(),true);
                    }

                    /*    System.out.println();
                        System.out.println("-------------");
                    for(Interval subInterval : subIntervals){
                        System.out.print(subInterval.toStringInterval() + ":" + intervalMap.get(subInterval.toStringInterval()) + " ");
                    }
                    System.out.println();
                    System.out.println("boolean key " + allTrueCheck);
                    System.out.println("-------------");
                    */

                }
                idx++;
            }
        }
        //System.out.println();
        //System.out.println("-------------");
        /*for(Interval interval : intervals){
            System.out.println(interval.toStringInterval() + " : " + intervalMap.get(interval.toStringInterval()));
        }*/
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        endTime = System.currentTimeMillis();
        System.out.println("\nTotal time taken: " + (endTime - startTime) + " milliseconds");

        if(isSorted(shuffledArray)){
            System.out.println("Array sorted.");
        }
        else{
            System.out.println("Array not sorted");
        }

        //System.out.println("Sorted Array:");
        //printShuffledArr(shuffledArray);
    }

    public static void printIntervals(List<Interval> intervals) {
        System.out.println("Intervals:");
        for (Interval interval : intervals) {
            String temp = "[" + interval.getStart() + ", " + interval.getEnd() + "]";
            System.out.println(temp);
        }
    }

    public static void printShuffledArr(int[] shuffledArray) {
        for (int i = 0; i < shuffledArray.length; i++) {
            System.out.print(shuffledArray[i] + " ");
        }
        System.out.println();
    }

    public static int[] generateAndShuffleArray(int N, Random random) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            list.add(i);
        }

        Collections.shuffle(list, random);

        int[] array = new int[N];
        for (int i = 0; i < N; i++) {
            array[i] = list.get(i);
        }

        return array;
    }

    public static List<Interval> generate_intervals(int start, int end) {
        List<Interval> frontier = new ArrayList<>();
        frontier.add(new Interval(start, end));

        int i = 0;
        while (i < frontier.size()) {
            int s = frontier.get(i).getStart();
            int e = frontier.get(i).getEnd();

            i++;

            if (s == e) {
                continue;
            }

            int m = s + (e - s) / 2;

            frontier.add(new Interval(m + 1, e));
            frontier.add(new Interval(s, m));
        }

        List<Interval> retval = new ArrayList<>();
        for (i = frontier.size() - 1; i >= 0; i--) {
            retval.add(frontier.get(i));
        }

        return retval;
    }

    public static void merge(int[] array, int s, int e) {
        int m = s + (e - s) / 2;
        int[] left = new int[m - s + 1];
        int[] right = new int[e - m];
        int l_ptr = 0, r_ptr = 0;
        for (int i = s; i <= e; i++) {
            if (i <= m) {
                left[l_ptr++] = array[i];
            } else {
                right[r_ptr++] = array[i];
            }
        }
        l_ptr = r_ptr = 0;

        for (int i = s; i <= e; i++) {
            if (l_ptr == m - s + 1) {
                array[i] = right[r_ptr++];
            } else if (r_ptr == e - m || left[l_ptr] <= right[r_ptr]) {
                array[i] = left[l_ptr++];
            } else {
                array[i] = right[r_ptr++];
            }
        }
    }

    public static boolean isSorted(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }
        return true;
    }
}

class Interval {
    private int start;
    private int end;

    public Interval(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String toStringInterval(){
        return "[" + start + ", " + end + "]";
    }
}
