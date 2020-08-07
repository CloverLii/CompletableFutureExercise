package completablefuture_assignment;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A CompletableFuture assignment template class
 * @Author Shaoqun Wu
 * 
 */
public class Mortality {
	protected static final String DIE_MSG = "Die at = ";
	protected static final String WORKING_YEARS_MSG = "Working years = ";
	protected static final String RETIREMENT_YEARS_MSG = "Retirement years = ";
	protected static final String SUPER_PAYOUT_MSG = "Super payout = %.1f (median salaries)";
	protected static final String LIFESTYLE_MSG = "You live on %.1f%% of median salary";
    protected static Random random = new Random();
    //use debug flag to see the threads in action
    public static boolean debug = false;
    
    /*
     *  a helper method that delays the execution of the current thread 
     */
    protected static void delay(){
		 try {
		  	  //delay a bit
	         Thread.sleep(random.nextInt(10)*100);		                   
	         } catch (InterruptedException e) {
	         throw new RuntimeException(e); }
	}

     /**
    	  * a Supplier that provides the retirement age between 60-70 (inclusive)
    	  */	 
	static Supplier<Integer> RetirementAgeSupplier = ()->{
	  String currentThreadName = Thread.currentThread().getName();
	  if(debug){
	      System.out.println("^^"+currentThreadName + "^^ Retrieving Retirement age ....");
		}
	   delay();
	   int retirementAge =   60 + new Random().nextInt(11);
	   if(debug){
	     System.out.println("^^"+currentThreadName + "^^ returned Retirement age: "+ retirementAge);
	   }
	   return retirementAge;  
    };
  
    /*
     *  Calculates the death age given a gender and birth year.
     *  @param gender ("male" or female)
     *  @param birthYear (between 1928 and 2018) 
     *  @return a death age
     */
    protected static int calculateDeathAge(int birthYear, String gender) {
		final int deathAge;
		if ("male".equals(gender)) {
			deathAge = 79 + (2018 - birthYear) / 20;
		} else {
			deathAge = 83 + (2018 - birthYear) / 30;
		}
		System.out.println(DIE_MSG + deathAge);
		return deathAge;
	}
    
    /*
     * Calculate the working years
     * @param retirementAge
     * @param superAge
     * @return the working years
     */
    protected static int calculateWorkingYears(int superAge, int retirmementAge) {
    	System.out.println(WORKING_YEARS_MSG + (retirmementAge - superAge));
    	return retirmementAge - superAge;
    }
    
    /*
     * Calculate retirement years
     * @param retirementAge
     * @param deathAge
     * @return the retirement years
     */
    protected static int calculateRetirementYears(int deathAge, int retirementAge) {
    	System.out.println(RETIREMENT_YEARS_MSG + (deathAge - retirementAge));
    	return deathAge - retirementAge;
    }
    
    /*
     * Calculate super balance
     * @param workingYears
     * @param strategy
     * @param contribution
     * @return the super balance
     */
    protected static double calculateSuperBalance(int workingYears, String strategy, int contribution) {
    	double superBalance = 0;  	
    	for (int i = 0; i< workingYears; i++) {
    		superBalance = superBalance * performance(strategy) + contribution/100.0;  
    	}
    	System.out.println(String.format(SUPER_PAYOUT_MSG, superBalance));	
    	return superBalance;
    }
    
    /*
     * Calculate lifestyle
     * @param retirementYears
     * @param superBalance
     * @return the lifestyle
     */
    protected static double calculateLifestyle(int retirementYears, double superBalance) {
    	
    	System.out.println(String.format(LIFESTYLE_MSG, (superBalance/retirementYears)*100.0));
    	return superBalance/retirementYears;
    }
    
    /*
     * calculate performance percentage given a strategy name
     * @param a strategy name
     * @return performance percentage (double)
     */
	protected static double performance(String strategy) {
		switch (strategy.toLowerCase()) {
		case "growth": return 1.045;
		case "balanced": return 1.035;
		case "conservative": return 1.025;
		case "cash": return 1.01;
		default: throw new RuntimeException("Unknown Super strategy: " + strategy);
		}
	}
	
	/**
	 * Displays the final output messages, given the final node of the dataflow graph.
	 *
	 * @param sependlevel calculated based on the performance, contribution and working years.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void displayResult(double spendLevel){
		// Get the final output of the dataflow calculation and analyze it.
		// Median income in New Zealand is $44,011.
		// See http://www.superannuation.asn.au/resources/retirement-standard
		// Modest single lifestyle requires $24,506, which is about 0.56 of median income.
		// Comfortable lifestyle requires $44,011, which is about same as median income.
		if (spendLevel < 0.56) {
			System.out.format("Miserable poverty...");
		} else if (spendLevel < 1.0) {
			System.out.format("A modest lifestyle...");
		} else {
			System.out.format("Comfortable!...");
		}
	}
	
	/*
	 * a method to show how "join" works 
	 * "join" method is blocking. 
	 *  Async calls are executed sequentially by one worker (thread) one at a time.
	 */
    static void testWithJoin(String fullname){
    	 System.out.println("JOIN method is blocking... (single thread)");
    	 CompletableFuture.supplyAsync(()-> PersonInfoSupplier.getPersonInfo(fullname)).join();	
		 CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getStartSuperAge).join();	
		 CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getSuperStrategy).join();
		 CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getContribution).join();
		 CompletableFuture.supplyAsync(RetirementAgeSupplier).join();	
    }
    
    /*
     * a method to show to how "get" works
	 * "get" method is also blocking. 
	 * Async calls are executed sequentially by one worker (thread) one at a time.
	 */
    static void testWithGet(String fullname){
     	System.out.println("\nGET method is blocking... (single thread)");
    	  try{
   		 CompletableFuture.supplyAsync(()-> PersonInfoSupplier.getPersonInfo(fullname)).get();	
   		 CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getStartSuperAge).get();	
   		 CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getSuperStrategy).get();
   		 CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getContribution).get();
   		 CompletableFuture.supplyAsync(RetirementAgeSupplier).get();
   		
            }
   	  catch(Exception e){}
    	
    }
    
    /*
     * a method to show how to make Async calls and use "join" at the end to wait for thread's returning
   	 * Async calls are executed asynchronously by more than one workers (threads).
   	 */
    static void testWithoutJoinAndGet(String fullname){
      	System.out.println("\nNon-blocking Async calls ... (multiple threads)");  
    	
    	     CompletableFuture[] cfutures = new CompletableFuture[5];
    	
    	     cfutures[0] = CompletableFuture.supplyAsync(()-> PersonInfoSupplier.getPersonInfo(fullname));	
    	     cfutures[1] = CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getStartSuperAge);	
    	     cfutures[2] = CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getSuperStrategy);
    	     cfutures[3] = CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getContribution);
    	     cfutures[4] = CompletableFuture.supplyAsync(RetirementAgeSupplier);
    	     
    	     CompletableFuture.allOf(cfutures).join();
  }
      
  /**
   *  
   * @param fullName a person's name 
   * (see all the names in the "fullname" file in the project directory)
   *  to search for
   */
  public static void query(String fullName){
	System.out.println(fullName);
	
	CompletableFuture<Integer> startSuperFuture = CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getStartSuperAge);
	System.out.println("    start super age = " + startSuperFuture.join());
	
	CompletableFuture<Integer> retirementAgeFuture = CompletableFuture.supplyAsync(RetirementAgeSupplier);
	System.out.println("    retirement age = " + retirementAgeFuture.join());
	
	// conduct comppletableFuture object to handle workingYears
	CompletableFuture<Integer> workingYearsFuture = startSuperFuture
			.thenCombine(retirementAgeFuture, (x, y) -> calculateWorkingYears(x, y));
	
	CompletableFuture<String> strategyFuture = CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getSuperStrategy);
	System.out.println("    super strategy = " + strategyFuture.join());
	
	CompletableFuture<Integer> contributionFuture = CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getContribution);
	System.out.println("    contribution% = " + contributionFuture.join());
	
	// combine three comppletableFuture objs into HashMap
	// ref: https://springorama.wordpress.com/2018/05/10/combining-merging-more-than-two-completablefutures-together/
	Map<Integer, Object> futuresMap = workingYearsFuture
			.thenCompose(workingYears ->strategyFuture
					.thenCombine(contributionFuture, (strategy, contribution) -> {
						Map<Integer, Object> myMap = new HashMap<>();
						myMap.put(1, new Integer(workingYears));
						myMap.put(2, new String(strategy));
						myMap.put(3, new Integer(contribution));
						return myMap;
			})).join();
	//System.out.println("********* combined hashmap: " + futuresMap.get(1) + "-" + futuresMap.get(2) + "-" + futuresMap.get(3));
		
	// conduct comppletableFuture object to handle superBalance
	CompletableFuture<Double> superBalanceFuture = CompletableFuture.supplyAsync(
			() -> calculateSuperBalance((int)futuresMap.get(1), (String)futuresMap.get(2), (int)futuresMap.get(3)));

	CompletableFuture<Optional<PersonInfo>> personInfoFuture = CompletableFuture.supplyAsync(()-> PersonInfoSupplier.getPersonInfo(fullName));
	System.out.println("    birth year = " + personInfoFuture.join().get().birthYear);
	System.out.println("    sex = " + personInfoFuture.join().get().gender);
	
	// conduct comppletableFuture object to handle lifestyle
	CompletableFuture lifestyleFuture = personInfoFuture
			.thenApply(person -> calculateDeathAge(person.get().birthYear, person.get().gender))  // calculate death age
			.thenCombine(retirementAgeFuture, (x, y) -> calculateRetirementYears(x, y))	 // calculate retirement years
			.thenCombine(superBalanceFuture, (x, y) -> calculateLifestyle(x, y))	// calculate lifestyle
			.thenAccept(spendLevel -> displayResult(spendLevel));	// no returned values
    }
    
	 public static void main(String[] args){
		 String fullName = "Mia Collins";
         //uncomment to experiment with "join" and "get" methods 	 
		 //testWithJoin(fullName);
		 //testWithGet(fullName);
		// testWithoutJoinAndGet(fullName);

		 query(fullName);
  }
}
