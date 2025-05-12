package main.java;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * The class that will control a Lab that has things like printers in it
 * 
 * @author Pedro RM (Dot)
 * @version 1.0
 */
public class Lab {

    public static final String EOL = System.lineSeparator();    // The System's line separator
    
    private final StringBuilder sb;     // The final StringBuilder we'll be using to announce the info of our Lab
    
    private final Printer[] printers;   // The printers present in our Lab

    /**
     * Initializes a Lab given a certain number of printers, indicated in our {@code inputFile}, and a StringBuilder we'll be using
     * 
     * @param inputFile The file from where we'll know how many printers will there be in our Lab
     * @param sb The StringBuilder where we'll be updating all our information about our Lab
     * @requires The inputFile to have only a positive integer value greater than 0 and {@code sb != null}
     * @throws FileNotFoundException if the input file can't be found
     */
    public Lab(String inputFile, StringBuilder sb) throws FileNotFoundException {
        
        // We must first initialize our main attributes
        this.sb = sb;
        
        // Including the initialization of every Printer depending on the value given in the inputFile
        Scanner sc = new Scanner(new File(inputFile));
        printers = new Printer[sc.nextInt()];
        initializePrinters(printers);
        
        // We must of course close the Scanner after use
        sc.close();
        
    }
    
    /**
     * Initializes every printer of a given empty array following the instructions given in the project
     * 
     * @param array The given array where we'll be initializing every Printer
     * @requires {@code array != null}
     */
    private void initializePrinters(Printer[] array) {
        
        // For each element of the array of Printers, we'll initialize the Printer
        for (int i = 0; i < array.length; i++) {
            array[i] = new Printer(i, this.sb);
        }
        
    }
    
    /**
     * Takes every order of a given input file
     * 
     * @param inputFile The input file from where we'll take the orders
     * @requires The input file to be well written as shown in the input examples
     * @throws FileNotFoundException if the input file can't be found
     */
    public void takeOrders(String inputFile) throws FileNotFoundException {
        
        // Firstly, we must initialize a Scanner to read each new order from the inputFile
        Scanner sc = new Scanner(new File(inputFile));
        String[] line;
        
        // For each line of the inputFile, we'll process the correspondent Job
        while (sc.hasNextLine()) {
            line = sc.nextLine().split(" ");
            processJob(new Job(line[2], Integer.parseInt(line[3]), Integer.parseInt(line[4]), Integer.parseInt(line[0]), Priority.valueOf(line[5])));
        }
        
        // We must of course close the Scanner after use
        sc.close();

        // Now, we'll display the message that every print jobs have been assigned to a Printer
        this.sb.append("All print jobs have been assigned to a printer!").append(EOL);
        
        // We'll now need to print every job remaining
        while (!isPrintingFinished()) {
            
            // We'll first get the printing duration of the current job of the first printer to finish a job
            int whenFirstJobFinish = this.printers[firstPrinterToFinishJob()].currentJob().getPrintingDuration() + this.printers[0].currentTime();
            
            // Then, we'll print until the calculated determined moment in time in each printer
            for (Printer p : printers) {
                p.printUntilTime(whenFirstJobFinish);
            }
        }
        
        // Now, we'll display the message that every print jobs have been processed
        this.sb.append("All printing jobs have been processed!");
        
    }

    /**
     * Process a given job by scheduling it to a certain Printer of our Lab
     * 
     * @param job The job to be processed
     * @requires {@code job != null}
     */
    public void processJob(Job job) {
        
        // We'll first check that the job's arrival time coincides with the current time
        while (job.getArrivalTime() != this.printers[0].currentTime()) {
            
            // If not, we'll get the printing duration of the current job of the first printer to finish a job
            int whenFirstJobFinish = this.printers[firstPrinterToFinishJob()].currentJob().getPrintingDuration() + this.printers[0].currentTime();
            
            // Depending on that duration, we'll be printing until a determined moment in time
            int time = (whenFirstJobFinish < job.getArrivalTime())? whenFirstJobFinish : job.getArrivalTime();
            
            // Then, we'll print until the calculated determined moment in time in each printer
            for (Printer p : printers) {
                p.printUntilTime(time);
            }
        }
        
        // Now, we'll add the job to the correspondent Printer
        int printerId = (job.getPriority() == Priority.HIGH)? firstPrinterToFinishHigh() : firstPrinterToFinish();
        this.printers[printerId].addJob(job);
        
        // And finally we'll update this information in our StringBuilder
        this.sb.append("[TIME: ").append(this.printers[0].currentTime()).append("] Job ")
               .append(job.getModelName()).append(" (").append(job.getPriority())
               .append(") scheduled to printer ").append(printerId).append(", printing will take ")
               .append(job.getPrintingDuration()).append(".").append(EOL);
        
    }

    /**
     * Returns the ID of the Printer that'll be first to end all its jobs
     * 
     * @return the ID of the Printer that'll be first to end all its jobs
     */
    public int firstPrinterToFinish() {
        
        // We start by creating a result variable  
        int min = 0;  

        // Together with the minimum time for now
        int minTotal = this.printers[min].timeToFinishHigh() + this.printers[min].timeToFinishNormal();  

        // We'll now iterate through all printers (excluding the number 0, because we're supposing that it's the fastest one for now)
        for (int i = 1; i < this.printers.length; i++) {  

            // We'll calculate the total time for the current Printer  
            int iTotal = this.printers[i].timeToFinishHigh() + this.printers[i].timeToFinishNormal();  

            if (iTotal < minTotal) {  
                // If this printer is faster, we'll update the result
                min = i;  
                minTotal = iTotal;  
            }  
        }  

        // We'll finally return the result
        return min;  

    }

    /**
     * Returns the ID of the Printer that'll be first to end all its high priority jobs (following the project instructions)
     * 
     * @return the ID of the Printer that'll be first to end all its high priority jobs (following the project instructions)
     */
    public int firstPrinterToFinishHigh() {
        
        // We start by creating a result variable
        int min = 0;
        
        // Together with the minimum times for now
        int minHighTime = this.printers[min].timeToFinishHigh();
        int minNormalTime = this.printers[min].timeToFinishNormal();
        
        // We'll go through each Printer (excluding the number 0, because we're supposing that it's the fastest one for now)
        for (int i = 1; i < this.printers.length; i++) {
            
            // We'll calculate the times of the selected Printer
            int iHighTime = this.printers[i].timeToFinishHigh();
            int iNormalTime = this.printers[i].timeToFinishNormal();
            
            if (iHighTime < minHighTime || iHighTime == minHighTime && iNormalTime < minNormalTime) {
                
                // If it's considered this "i" printer faster to finish high priority jobs, then we'll update our result variable
                min = i;
                
                // And we'll also update our minimum times
                minHighTime = iHighTime;
                minNormalTime = iNormalTime;
            }
        }
        
        // We can finally return our result
        return min;
    }

    /**
     * Returns the ID of the Printer that'll be first to end its current or, if there isn't any, next job
     * 
     * @return the ID of the Printer that'll be first to end its current or, if there isn't any, next job
     */
    public int firstPrinterToFinishJob() {
        
        // We'll start by creating a result variable
        int min = -1;
        
        // Together with the minimum time for now
        int minCurrJobTime = Integer.MAX_VALUE;
        
        // We'll go through each Printer
        for (int i = 0; i < this.printers.length; i++) {
            
            if (!this.printers[i].isEmpty()) {
            
                // We'll get the current job printing time of the selected Printer
                int iCurrJobTime = this.printers[i].currentJob().getPrintingDuration();
                
                if (iCurrJobTime < minCurrJobTime || min == -1) {
                    
                    // If it's considered this "i" printer faster to finish the current Job, then we'll update our result variable
                    min = i;
                    
                    // And we'll also update our minimum time
                    minCurrJobTime = iCurrJobTime;
                }
                
            }
            
        }
        
        // We can finally return our result
        return (min == -1)? 0 : min;
    }

    /**
     * Returns whether every of our printers is empty or not
     * 
     * @return {@code true} if every printer is empty, {@code false} otherwise
     */
    public boolean isPrintingFinished() {
        
        // We'll go through each printer
        for (Printer p : this.printers) {
            
            // If we found any that isn't empty, we'll return false
            if (!p.isEmpty()) {
                return false;
            }
            
        }
        
        // If no non empty Printer was found, we'll return true
        return true;
    }
}
