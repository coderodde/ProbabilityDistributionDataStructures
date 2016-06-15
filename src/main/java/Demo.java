
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.coderodde.stat.AbstractProbabilityDistribution;
import net.coderodde.stat.support.ArrayProbabilityDistribution;
import net.coderodde.stat.support.BinaryTreeProbabilityDistribution;
import net.coderodde.stat.support.LinkedListProbabilityDistribution;

public class Demo {

    private static final int DISTRIBUTION_SIZE = 100_000;
    
    public static void main(final String[] args) {
//        BinaryTreeProbabilityDistribution<Integer> dist = 
//                new BinaryTreeProbabilityDistribution<>();
//        
//        for (int i = 0; i < 8; ++i) {
//            dist.addElement(i, 0.1 * (i + 1));
//        }
//       
//        System.out.println(dist.debugToString());
//        
//        dist.removeElement(3);
//        dist.removeElement(2);
//        dist.removeElement(5);
//        
//        System.out.println(dist.debugToString());
//        
//        for (int i = 0; i < 10; ++i) {
//            System.out.println(dist.sampleElement());
//            
//        }
        
        System.out.println("[STATUS] Warming up...");
        warmup();
        System.out.println("[STATUS] Warming up done!");
        System.out.println();
        
        AbstractProbabilityDistribution<Integer> arraypd = 
                new ArrayProbabilityDistribution<>();
        
        AbstractProbabilityDistribution<Integer> listpd = 
                new LinkedListProbabilityDistribution<>();
        
        AbstractProbabilityDistribution<Integer> treepd =
                new BinaryTreeProbabilityDistribution<>();
        
        profile(arraypd);
        profile(listpd);
        profile(treepd);
    }
    
    private static void 
        profile(final AbstractProbabilityDistribution<Integer> pd) {
        final Random random = new Random();
        
        System.out.println("[" + pd.getClass().getSimpleName() + "]:");
        
        long totalDuration = 0L;
        
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < DISTRIBUTION_SIZE; ++i) {
            pd.addElement(i, 10.0 * random.nextDouble());
        }
        
        long endTime = System.currentTimeMillis();
        
        System.out.println("addElement() in " + (endTime - startTime) + 
                           " milliseconds.");
        totalDuration += (endTime - startTime);
        
        startTime = System.currentTimeMillis();
        
        for (int i = 0; i < DISTRIBUTION_SIZE; ++i) {
            pd.sampleElement();
        }
        
        endTime = System.currentTimeMillis();
        
        System.out.println("sampleElement() in " + (endTime - startTime) +
                           " milliseconds.");
        totalDuration += (endTime - startTime);
        
        final List<Integer> contents = new ArrayList<>(DISTRIBUTION_SIZE);
        
        for (int i = 0; i < DISTRIBUTION_SIZE; ++i) {
            contents.add(i);
        }
        
        shuffle(contents);
        
        startTime = System.currentTimeMillis();
        
        for (Integer i : contents) {
            pd.removeElement(i);
        }
        
        endTime = System.currentTimeMillis();
        
        System.out.println("removeElement() in " + (endTime - startTime) + 
                           " milliseconds.");
        
        totalDuration += (endTime - startTime);
        
        System.out.println("Total duration: " + totalDuration + 
                           " milliseconds.");
        
        System.out.println();
    }
    
    private static void shuffle(final List<Integer> list) {
        final Random random = new Random();
        
        for (int i = 0; i < list.size(); ++i) {
            final int index = random.nextInt(list.size());
            final Integer integer = list.get(index);
            list.set(index, list.get(i));
            list.set(i, integer);
        }
    }
        
    private static void warmup() {
        final long seed =35214717058750L; System.nanoTime();
        final Random inputRandom1 = new Random(seed);
        final Random inputRandom2 = new Random(seed);
        final Random inputRandom3 = new Random(seed);
        
        final AbstractProbabilityDistribution<Integer> pd1 = 
             new ArrayProbabilityDistribution<>(inputRandom1);
        
        final AbstractProbabilityDistribution<Integer> pd2 = 
             new LinkedListProbabilityDistribution<>(inputRandom2);
        
        final AbstractProbabilityDistribution<Integer> pd3 =
             new BinaryTreeProbabilityDistribution<>(inputRandom3);
        
        final Random random = new Random(seed);
        final List<Integer> content = new ArrayList<>();
        
        System.out.println("Seed = " + seed);
        
        for (int iteration = 0; iteration < 100_000; ++iteration) {
            final double coin = random.nextDouble();
            
            if (coin < 0.3) {
                // Add a new element.
                final Integer element = random.nextInt();
                final double weight = 30.0 * random.nextDouble();
                content.add(element);
                
                pd1.addElement(element, weight);
                pd2.addElement(element, weight);
                pd3.addElement(element, weight);
            } else if (coin < 0.5) {
                // Remove an element.
                if (!pd1.isEmpty()) {
                    final Integer element = choose(content, random);

                    pd1.removeElement(element);
                    pd2.removeElement(element);
                    pd3.removeElement(element);
                    content.remove(element);
                }
            } else if (!pd1.isEmpty()) {
                // Sample an element.
                final Integer element1 = pd1.sampleElement();
                final Integer element2 = pd2.sampleElement();
                
                if (!element1.equals(element2)) {
                    throw new IllegalStateException(
                            "Identical probability distributions disagreed: " +
                            element1 + " vs. " + element2);
                }
                
                final Integer element3 = pd3.sampleElement();
            }
        }
    }
    
    private static Integer choose(final List<Integer> list, 
                                  final Random random) {
        return list.get(random.nextInt(list.size()));
    }
}
