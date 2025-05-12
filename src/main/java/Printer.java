package main.java;

import main.resources.ArrayQueue;
import main.resources.Queue;

/**
 * The class that manages every Printer of our project
 * 
 * @author Pedro RM (Dot)
 * @version 1.0
 */
public class Printer {
    
    public static final String EOL = System.lineSeparator();    // The System's line separator
    
    private final StringBuilder sb;     // The final StringBuilder we'll be using to announce the info of our Printer
    
    private final int printerId;        // The ID of our Printer that'll be using all the time    
    
    // We must create the queues of the different kinds of job's priorities
    private final Queue<Job> normalPriorityQueue = new ArrayQueue<>();
    private final Queue<Job> highPriorityQueue = new ArrayQueue<>();
    
    private int currentTime;    // We'll also be keeping track of the current time

    /**
     * Initialize a print with a given ID and StringBuilder (where the all the info will be sent to)
     * 
     * @param printerId The ID that will have our printer from now on
     * @param sb The StringBuilder that we'll use to keep the info of this printer transmitted
     * @requires {@code sb != null}
     * @ensures The info that this printer is online will be sent through the given StringBuilder
     */
    public Printer(int printerId, StringBuilder sb) {
        
        // We'll initialize our attributes
        this.sb = sb;
        this.printerId = printerId;
        this.currentTime = 0;
        
        // And finally we'll announce that the Printer has been initialized correctly
        this.sb.append("[TIME: ").append(currentTime())
               .append("] Printer ").append(getPrinterId())
               .append(" online, ready to take jobs.").append(EOL);
        
    }

    /**
     * Returns the ID of our Printer
     * 
     * @return The ID of our Printer
     */
    public int getPrinterId() {
        return this.printerId;  // We'll return the Printer's ID
    }

    /**
     * Returns the current time of our Printer
     * 
     * @return The current time of our Printer
     */
    public int currentTime() {
        return this.currentTime;        // We'll return the Printer's current time
    }

    /**
     * Returns the time remaining to finish the printing of all high priority jobs
     * 
     * @return the time remaining to finish the printing of all high priority jobs
     * @ensures {@code \result >= 0}
     */
    public int timeToFinishHigh() {
        return timeToFinishJobQueue(this.highPriorityQueue);    // We'll return the time to finish the full high priority queue
    }

    /**
     * Returns the time remaining to finish the printing of all normal priority jobs
     * 
     * @return the time remaining to finish the printing of all normal priority jobs
     * @ensures {@code \result >= 0}
     */
    public int timeToFinishNormal() {
        return timeToFinishJobQueue(this.normalPriorityQueue);  // We'll return the time to finish the full normal priority queue
    }
    
    /**
     * Returns the time remaining to finish the printing of all jobs in a certain queue
     * 
     * @param queue The queue we want to inspect
     * @requires {@code queue != null} and every {@code Job} in the Queue to have the arrival time set as {@code currentTime()}
     * @return the time remaining to finish the printing of all jobs in a certain queue
     * @ensures {@code \result >= 0}
     */
    private static int timeToFinishJobQueue(Queue<Job> queue) {
        
        // We'll start by initializing the variables we'll be using
        int result = 0;
        int size = queue.size();
        Job job;
        
        // We'll iterate through each Job in the queue
        for (int i = 0; i < size; i++) {
            
            // And in every single Job, we'll first store it
            job = queue.front();
            
            // Increment the result variable (that it's the total time)
            result += job.getPrintingDuration();
            
            // And finally dequeue and enqueue it in the Queue. This is because of the Queue's FIFO structure
            queue.dequeue();
            queue.enqueue(job);
        }
        
        // We'll finally return our result
        return result;
    }

    /**
     * Adds a job to the correspondent queue, depending on its priority
     * 
     * @param job The job we want our printer to print
     * @requires {@code job != null}
     */
    public void addJob(Job job) {
        
        // If the there's no current job, it means this new one will be automatically in status printing
        if (currentJob() == null) {
            job.setStatus(Status.PRINTING);
        }
        
        // Then, depending on the job's priority, we'll add it to the correspondent Queue
        if (job.getPriority() == Priority.HIGH) {
            this.highPriorityQueue.enqueue(job);
        }
        else {
            this.normalPriorityQueue.enqueue(job);
        }
        
    }

    /**
     * Returns the current job that's being printed (if there isn't any, the one that will be printed next)
     * 
     * @return the current job that's being printed (if there isn't any, the one that will be printed next)
     * @ensures {@code \result == null} if the printer doesn't have any jobs
     */
    public Job currentJob() {
        // We'll return the current high priority job or, if it doesn't exist, the normal priority job
        return (this.highPriorityQueue.isEmpty())? this.normalPriorityQueue.front() : this.highPriorityQueue.front();
    }

    /**
     * Prints the current job for a given time, if there's any
     * 
     * @param duration The time we want our Printer to be printing
     * @requires {@code !isEmpty() && duration <= currentJob().getPrintingDuration()}
     * @ensures {@code this.currentTime} will be updated accordingly to the given time (unless there's no current job)
     */
    public void printCurrentJob(int duration) {
        
        // In this case, I'm opting for a defensive method, instead of by contract...
        // Unless the current Job is null, we'll continue with our algorithm
        if (currentJob() != null) {
            
            // We start by incrementing the current time
            this.currentTime += duration;
            
            // Also, we'll set the current job printing duration
            int currJobPrintingDuration = currentJob().getPrintingDuration();
            
            // Finally, if it's supposed the job to end, we'll end it
            if (duration == currJobPrintingDuration) {
                finishCurrentJob();
            }
            else {
                currentJob().setPrintingDuration(currJobPrintingDuration - duration);
            }
            
        }
        
    }

    /**
     * Finishes the current job of our printer
     * 
     * @requires {@code !isEmpty()}
     */
    public void finishCurrentJob() {
        
        // We'll mark our current Job as finished
        currentJob().setStatus(Status.FINISHED);
        
        // Also, we'll announce that the current Job has finished printing
        this.sb.append("[TIME: ").append(currentTime()).append("] Job ")
               .append(currentJob().getModelName()).append(" has finished printing. Total wait time: ")
               .append(currentTime() - currentJob().getArrivalTime()).append(".").append(EOL);

        // And remove it from the correspondent Queue
        if (currentJob().getPriority() == Priority.HIGH) {
            this.highPriorityQueue.dequeue();
        }
        else {
            this.normalPriorityQueue.dequeue();
        }
        
        // After all this, if the current Job isn't null (so if there's any next job on our Printer), we'll mark it as printing
        if (currentJob() != null) {
            currentJob().setStatus(Status.PRINTING);
        }
        
    }

    /**
     * Returns whether our printer is empty of jobs or not
     * 
     * @return {@code true} if there's no other job available in our printer, {@code false} otherwise
     */
    public boolean isEmpty() {
        // We'll return whether all of our queues are empty or not
        return this.highPriorityQueue.isEmpty() && this.normalPriorityQueue.isEmpty();
    }

    /**
     * Prints for a given time everything possible. If there's no Job available, it will simply increment the current time
     * 
     * @param duration The given duration for the printer to be working
     * @requires {@code duration >= 0}
     * @ensures {@code this.currentTime} will be updated accordingly to the given time
     */
    public void printForDuration(int duration) {
        
        // We'll need to check if there's any job that we can print
        if (currentJob() != null) {
            
            // If yes, we'll then compare the given duration with the printing duration of the current job
            if (duration > currentJob().getPrintingDuration()) {
                
                // If the given duration is too much, we'll opt for recursion of this method
                // We'll set the printing duration of the current Job
                int jobPrintingDuration = currentJob().getPrintingDuration();
                
                // And then use it to finish the current job and use this method recursively with a lower duration
                printCurrentJob(jobPrintingDuration);
                printForDuration(duration - jobPrintingDuration);
                
            }
            else {
                // If the given duration doesn't surpass the current Job's printing duration, we'll simply print the job for the given time
                printCurrentJob(duration);
            }
            
        }
        else {
            // If not, we'll simply increment the current time
            this.currentTime += duration;
        }
        
    }

    /**
     * Prints until a given time everything possible. If there's no Job available, it will simply increment the current time
     * 
     * @param time The given time until when we want our printer to be working
     * @requires {@code time >= currentTime()}
     * @ensures {@code this.currentTime} will be updated accordingly to the given time
     */
    public void printUntilTime(int time) {
        printForDuration(time - currentTime()); // We'll forward the responsibility of this method to another one, in order to have a clearer code
    }
}
