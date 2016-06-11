
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.coderodde.stat.AbstractProbabilityDistribution;
import net.coderodde.stat.support.ArrayProbabilityDistribution;
import net.coderodde.stat.support.LinkedListProbabilityDistribution;

public class Demo {

    public static void main(final String[] args) {
        System.out.println("[STATUS] Warming up...");
        warmup();
        System.out.println("[STATUS] Warming up done!");
    }
    
    private static void warmup() {
        final long seed =35214717058750L; System.nanoTime();
        final Random inputRandom1 = new Random(seed);
        final Random inputRandom2 = new Random(seed);
        
        final AbstractProbabilityDistribution<Integer> pd1 = 
             new ArrayProbabilityDistribution<>(inputRandom1);
        
        final AbstractProbabilityDistribution<Integer> pd2 = 
             new LinkedListProbabilityDistribution<>(inputRandom2);
        
        final Random random = new Random(seed);
        final List<Integer> content = new ArrayList<>();
        
        System.out.println("Seed = " + seed);
        
        for (int iteration = 0; iteration < 10_000; ++iteration) {
            final double coin = random.nextDouble();
            
            if (coin < 0.3) {
                // Add a new element.
                final Integer element = random.nextInt();
                final double weight = 30.0 * random.nextDouble();
                content.add(element);
                
                pd1.addElement(element, weight);
                pd2.addElement(element, weight);
            } else if (coin < 0.5) {
                // Remove an element.
                if (!pd1.isEmpty()) {
                    final Integer element = choose(content, random);

                    pd1.removeElement(element);
                    pd2.removeElement(element);
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
            }
        }
    }
    
    private static Integer choose(final List<Integer> list, 
                                  final Random random) {
        return list.get(random.nextInt(list.size()));
    }
}
