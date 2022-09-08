/*
 * Name: Josh Naar
 * Course: CIS 421 - Artificial Intelligence
 * Assignment: 2
 * Due: 10/3/16
 */

import java.io.*;
import java.util.*;

public class NQueens {

    // Parameter: args[0] - the size of the board
    // Postcondition: Text file "prog2_log.txt" is created in same directory
    //                as this program 
    public static void main (String[] args) {

        // Get n, the length of each side of the board, from arguments        
        int n = Integer.parseInt(args[0]);

        // Generate the initial population
        ArrayList<int[]> population = createPopulation(n);

        // Initialize while-loop conditions
        boolean foundSolution = false;
        int generation = 0;

        // Store the solution for printing
        int[] solution = new int[0];

        while((foundSolution == false) && (generation <= 1000)) {

            // Check current population for a solution
            for (int i = 0; i < population.size(); i++) {
                if (calculateFitness(population.get(i)) >= 1.0) {
                    foundSolution = true;
                    solution = population.get(i);
                }
            }

            // Generate the mating pool from the population
            int[] [] matingPool = createMatingPool(population, n);

            // Generate offspring of mating pool using crossover
            int[] [] offspring = createOffspring(matingPool, n);

            // Decide whether to perform a mutation on each child
            for (int i = 0; i < offspring.length; i++) {
                offspring[i] = performMutation(offspring[i]);
            }

            // Remove the individuals with lowest fitness and
            // add all of the children to the population
            population = updatePopulation(population, offspring);

            // Visual progress indicator, useful when run on large N values
            if (generation % 100 == 0) {
                System.out.println("Generation " + generation + "...");
            }

            // Provide an accurate generation count
            if (foundSolution == false) {
                generation++;
            }

        }

        // Write results to output file
        // Set to append for multiple runs
        try(FileWriter fw = new FileWriter("prog2_log.txt", true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw)) {
            // Display results
            if (foundSolution) {
                out.println("Solution found in generation " + generation + 
                            ": " + Arrays.toString(solution));
            } else {
                out.println("No solution found");
            }

        } catch (IOException e) {
            System.out.println("Unexpected Error");
        }

    } 

    // Paremeter: n - the length of each genotype and 1/10 the number
    //                of genotypes to be generated
    // Returns: A population of n*10 genotypes
    public static ArrayList<int[]> createPopulation(int n) {
        ArrayList<int[]> population = new ArrayList<int[]>();
        for(int i = 0; i < n * 10; i++) {
            int [] genotype = new int[n];
            for (int j = 0; j < n; j++) {
                genotype[j] = j + 1;
            }
            population.add(shuffleArray(genotype));
        }
        return population;
    }

    // Parameters: population - ArrayList of all genotypes in population
    //             offspring - Array of child genotypes
    public static ArrayList<int[]> updatePopulation
        (ArrayList<int[]> population, int[] [] offspring) {
        int lowest = 0;
        // Remove a number of the lowest fitness individuals in the
        // population, equal to the number of created children
        for (int i = 0; i < offspring.length; i++) {
            for (int j = 0; j < population.size(); j++) {
                if (calculateFitness(population.get(lowest)) > 
                    calculateFitness(population.get(j))) {
                    lowest = j;
                } 
            }
            population.remove(lowest);
            lowest = 0;
        }
        // Add all children to population
        for (int i = 0; i < offspring.length; i++) {
            population.add(offspring[i]);
        }
        return population;
    }

    // Parameters: population - an ArrayList of genotypes
    //             n - the length of each genotype
    // Returns: an array of parent genotypes
    public static int[] [] createMatingPool(ArrayList<int[]> population, 
                                            int n) {
        // Ensure that there is always an even number of parents
        int parentCount = population.size() / 10;
        if (parentCount % 2 == 1) {
            parentCount++;
        }

        // Initialize variables for parent selection
        int[] [] matingPool = new int[parentCount] [n];
        Random rand = new Random();
        int currentMember = 0;

        // Choose 3 members randomly, find the one with the greatest fitness, 
        //   and add it to the mating pool
        // Do this until the size of the mating pool is equal to 10% of the 
        //   population (10%+1 if n is odd)
        while (currentMember < matingPool.length) {
            // Using sampling with replacement
            int[] parentOne = population.get(rand.nextInt(population.size()));
            int[] parentTwo = population.get(rand.nextInt(population.size()));
            int[] parentThree = population.get(rand.nextInt(
                                    population.size()));
            // Add the parent with greatest fitness to the mating pool
            if (calculateFitness(parentOne) >= calculateFitness(parentTwo) && 
               calculateFitness(parentOne) >= calculateFitness(parentThree)) {
                matingPool[currentMember] = parentOne;
            } else if (calculateFitness(parentTwo) >= calculateFitness
                      (parentOne) && calculateFitness(parentTwo) >= 
                       calculateFitness(parentThree)) {
                matingPool[currentMember] = parentTwo;
            } else if (calculateFitness(parentThree) >= calculateFitness
                      (parentOne) && calculateFitness(parentThree) >= 
                       calculateFitness(parentTwo)) {
                matingPool[currentMember] = parentThree;
            }
            currentMember++;
        }
        return matingPool;
    }

    public static int[] [] createOffspring(int[] [] matingPool, int n) {
        Random rand = new Random();

        // Store created offspring in an array
        int[] [] offspring = new int[matingPool.length] [n];

        // Initialize crossover variable outside of loop for correct
        // randomization behavior
        int crossover;

        // Crossover algorithm
        // Parents are paired based on the order they were chosen in
        for (int i = 0; i < n; i += 2) {
            crossover = rand.nextInt(n);
            // Copy the first n/2 units from parents, starting at crossover,
            // into the first n/2 positions of the children
	    for (int j = 0; j < n / 2; j++) {
                offspring[i] [j] = matingPool [i] [crossover];
                offspring[i+1] [j] = matingPool [i+1] [crossover];
                crossover++;
                // Wrap back to beginning if end of genotype is reached
                crossover = crossover % n; 
            }

            // Set second half of the children by copying all non-repeating             	    
            // values of the parent in, reading the parent from left to right
            //   offspring[i] is child 1, offspring[i+1] is child 2
            //	 matingPool[i] is parent 1, matingPool[i+1] is parent 2			    
            for (int j = n / 2; j < n; j++) {
                if (offspring[i+1] [j] == 0) {
                    for (int k = matingPool[i].length - 1; k >= 0; k--) {
                        boolean containsX = false;
                        // Look at each of the parent's values and see if
                        // it is already in the child
                        for (int l = 0; l < offspring[i+1].length; l++) {
                            if (offspring[i+1][l] == matingPool[i][k]) {
                                containsX = true;
                            }
                        }
                        // If the value is not already in the child,
                        // put it there
                        if (!containsX) {
                            offspring[i+1][j] = matingPool[i][k];
                        }
                    }
                }    

                // Same algorithm as immediately above, but with calls to 
                // parent 2 and child 1 instead of parent 1 and child 2
                if (offspring[i] [j] == 0) {
                    for (int k = matingPool[i+1].length - 1; k >= 0; k--) {
                        boolean containsX = false;
                        for (int l = 0; l < offspring[i].length; l++) {
                            if (offspring[i][l] == matingPool[i+1][k]) {
                                containsX = true;
                            }
                        }
                        if (!containsX) {
                            offspring[i][j] = matingPool[i+1][k];
                        }
                    }
                }  
            }
        }
        return offspring;
    }

    // Parameter: arr - an array of Integers
    // Returns: array with same values of arr in a new random order
    public static int[] shuffleArray(int[] arr) {
        // Basic implementation of the Fisher-Yates shuffle
        Random rand = new Random();
        for (int i = arr.length - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);
            int a = arr[index];
            arr[index] = arr[i];
            arr[i] = a;
        }
        return arr;
    }

    // Parameter: arr - the genotype to evaluate fitness of
    // Returns: the fitness as a double
    public static double calculateFitness(int[] arr) {
        int conflicts = countConflicts(arr);
        return (1 / (conflicts + 0.00001));
    }

    // Parameter: arr - the Integer array that will be checked for conflicts
    // Returns: the number of conflicts in the array
    public static int countConflicts(int[] arr) {
        int count = 0;
        // Calculate the slope between each pair of queens
        // If the absolute value of the slope is 1.0, there is a conflict
        for (int i = 0; i < arr.length-1; i++) {
            for (int j = i+1; j < arr.length; j++) {
                if (Math.abs((double)(arr[i] - arr[j])/ (i-j)) == 1.0) {
                    count++;
                }
            }
        }
        // Return the number of conflicts
        return count;
    }

    // Parameter: arr - a genotype that may be mutated
    // Returns: the genotype, either mutated or the same
    public static int [] performMutation(int[] arr) {
        Random rand = new Random();
        int mutate = rand.nextInt(10);
        int pos1;
        int pos2;
        int hold;
        // Genotype has a 10% chance of being mutated
        if (mutate == 0) {
            pos1 = rand.nextInt(arr.length);
            pos2 = rand.nextInt(arr.length);
            // Make sure that the same swap position isn't chosen twice
            while (pos2 == pos1) {
                pos2 = rand.nextInt(arr.length);
            }
            hold = arr[pos1];
            arr[pos1] = arr[pos2];
            arr[pos2] = hold;
        }
        return arr;
    }

}

/*

Currently:

	-Finished!

Going forward:

	-Run 25x on N = 12, generate log file
        -Create scatter plot from the generated data

*/

