package edu.cmu.lti.ram_wordnet;


import java.text.NumberFormat;

/**
 * 
 * @author Hideki Shima
 *
 */
public class MemoryMonitor {
  private static final Runtime runtime = Runtime.getRuntime();
  private Long before = 0L;
  private NumberFormat nf = NumberFormat.getNumberInstance();
  
  public MemoryMonitor() {
    measure();
  }
  
  public String measure() {
    runGC();
    Long current = getUsedMemory();
    Long delta = current - before;
    before = current;
    return nf.format(delta);
  }

  public String currentTotal() {
    runGC();
    return nf.format(runtime.totalMemory());
  }
  
  private static long getUsedMemory() {
    return runtime.totalMemory() - runtime.freeMemory();
  }

  private static void runGC() {
    // It helps to call Runtime.gc()
    // using several method calls:
    for (int r = 0; r < 4; ++r)
      _runGC();
  }
  
  @SuppressWarnings("static-access")
  private static void _runGC() {
    long usedMem1 = getUsedMemory(), usedMem2 = Long.MAX_VALUE;
    for (int i = 0; (usedMem1 < usedMem2) && (i < 500); ++i) {
      Thread.currentThread().yield();
      runtime.runFinalization();
      runtime.gc();
      usedMem2 = usedMem1;
      usedMem1 = getUsedMemory();
    }
  }
  
}
