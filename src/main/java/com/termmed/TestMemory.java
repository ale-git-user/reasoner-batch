package com.termmed;

public class TestMemory {
    
	static long mUsed;
	private static int mb=1024;
	private static Runtime runtime = Runtime.getRuntime();
    public static void test() {
         
         
        System.out.println("##### Heap utilization statistics [MB] #####");
         
        //Print used memory
        System.out.println("Used Memory (Kb):"
            + (runtime.totalMemory() - runtime.freeMemory()) / mb);
 
        //Print free memory
        System.out.println("Free Memory (Kb):"
            + runtime.freeMemory() / mb);
         
        //Print total available memory
        System.out.println("Total Memory (Kb):" + runtime.totalMemory() / mb);
 
        //Print Maximum available memory
        System.out.println("Max Memory (Kb):" + runtime.maxMemory() / mb);
    }
    public static void getMemRetainedAtThisMoment(String comment){
    	 System.out.println(comment + "- Retained Memory (Kb):"
    	            + (runtime.totalMemory() - runtime.freeMemory() - mUsed) / mb);
    	 
    }
    public static void updateMemUsedAtThisMoment(String comment){
    	long newMemUsed=runtime.totalMemory() - runtime.freeMemory();

   	 	System.out.println(comment + "- Used Memory at this moment (Kb):"
	            + (newMemUsed/mb));
    	if (mUsed!=0){
    		System.out.println("    Diff initial used memory VS previous value:"
    	            + ((newMemUsed -mUsed)/mb));
    	}
    	mUsed=newMemUsed;
    }
}