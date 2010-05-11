/*
 * @author: Jesse Sanford
 * @description: The following is a java implementation of the Operating Systems HW1
 * @credits: portions of this were taken from the following articles
 *     http://littletutorials.com/2008/03/14/console-applications-with-java-6/
 *
 */
package homework3;

import java.io.Console;

import java.util.*;

public class Main {

    private static final String INSTRUCTIONS = "Welcome to the system. Please answer the following questions about this system:\n";
    private static final String LAMBDA = "Please enter the value for lambda (Default 0)\n";
    private static final String ALPHA = "Please enter the value for alpha. Value must be between 0 and 1 inclusive. (Default 0.1)\n";
    private static final String NUM_CDROMS = "# of CD/RW drives? (Default 1)\n";
    private static final String NUM_DISKS = "# of Disk drives? (Default 1)\n";
    private static final String NUM_PRINTERS = "# of Printers? (Default 1)\n";
    private static final String TOTAL_MEMORY = "Please enter the total memory size (in words) of the system? It must be an integer number that is a power of two. (Default 2)";
    private static final String PAGE_SIZE = "What is the size of a page (in words)? It must be an integer number that is a power of two. (Default 2)";
    //private static final String MAX_PROCESS_SIZE = "What is the maximum size of a process? This should be an integer number of frames.";
    private static final String PROCESS_TOO_BIG = "Sorry that process is larger than the memory on this machine. Try a smaller process.";
    private static final String SYSGEN_FINISHED = "Sysgen is finished now running.\n";
    private static final String TIME_SPENT_ON_CPU = "Please enter the time in milliseconds the last process spent on the cpu.\n";

    private static final String PROCESS_SIZE = "What is the size of this process? This should be integer number of pages or frames. Must also be > 0 and < %1$s ";
    private static final String UNKNOWN_COMMAND = "Unknown command [%1$s]\n";
    private static final String COMMAND_ERROR = "Command error [%1$s]: [%2$s]\n";
    private static final String INPUT_NEEDED = "Please enter the [%1$s]\n";
    private static final String TIME_FORMAT = "%1$tH:%1$tM:%1$tS";
    private static final String PROMPT = TIME_FORMAT + " $ ";
    
    private static int lambda = 0;
    private static double alpha = 0.1;
    private static int sizeOfMemory = 2; //size in number of words must be power of 2
    private static Vector<Integer[]> frameTable = new Vector<Integer[]>(0); // this should have a cell for each word
                                                           // so it's length should equal the sizeOfMemory
                                                           // just mark the cell's as used when they in use
                                                           // by putting the id of the process useing them
                                                           // into the cell.
    private static int sizeOfPage = 2; //size in number words must be power of 2
    private static int numFreePages = 1;
    //private static int maxProcessSize = 0; //this should be in number of frames
    private static Vector<String> allowedCommands = new Vector<String>(2);
    private static int numPrinters = 1;
    private static int numDisks = 1;
    private static int numCdroms = 1;
    private static Vector<PCB> jobPool = new Vector<PCB>(0);
    private static Vector<PCB> interactiveJobPool = new Vector<PCB>(0);
    private static Vector<PCB> batchJobPool = new Vector<PCB>(0);
    private static Vector<PCB> interactiveReadyQueue = new Vector<PCB>(0);
    private static Vector<PCB> batchReadyQueue = new Vector<PCB>(0);
    private static Vector<PCB> allActivePids = new Vector<PCB>(0);
    private static Vector<Vector<PCB>> printerQueues = new Vector<Vector<PCB>>(0);
    private static Vector<Vector<PCB>> diskQueues = new Vector<Vector<PCB>>(0);
    private static Vector<Vector<PCB>> cdromQueues = new Vector<Vector<PCB>>(0);
    private static boolean running = false;
    private static int pidCounter = 0;
    private static PCB currentPCB = null;
    private static double totalCpuTimeOfAllCompletedProccesses = 0.0;
    //private static int interactiveRoundRobinCounter = 0;
    private static String globalQueuePriority = "batch";

    private static boolean execSysgen(Console console) {
        boolean finishedSysgen = false;

        allowedCommands.add("A");
        allowedCommands.add("B");
        allowedCommands.add("t");
        allowedCommands.add("X");
        allowedCommands.add("T");
        allowedCommands.add("K");
        allowedCommands.add("S");
        allowedCommands.add("Z");

        console.printf(INSTRUCTIONS);

        console.printf(TOTAL_MEMORY);
        try {
            sizeOfMemory = Integer.parseInt(console.readLine(PROMPT, new Date()));
            //check to make sure it is a power of 2
            while(!((sizeOfMemory!=0) && (sizeOfMemory&(sizeOfMemory-1))==0)){
                console.printf(TOTAL_MEMORY);
                sizeOfMemory = Integer.parseInt(console.readLine(PROMPT, new Date()));
            }
        } catch (Exception e) {
            sizeOfMemory = 2;
        }

        console.printf(PAGE_SIZE);
        try {
            sizeOfPage = Integer.parseInt(console.readLine(PROMPT, new Date()));
            //check to make sure it is a power of 2
            while(!((sizeOfPage!=0) && (sizeOfPage&(sizeOfPage-1))==0)){
                console.printf(PAGE_SIZE);
                sizeOfPage = Integer.parseInt(console.readLine(PROMPT, new Date()));
            }
        } catch (Exception e) {
            sizeOfPage = 2;
        }

        //re-calculate the number of free pages;
        numFreePages = (int)sizeOfMemory/sizeOfPage;
        //grow the frame table to meet the needed capacity
        frameTable.setSize(numFreePages);

        /*console.printf(MAX_PROCESS_SIZE);
        try {
            maxProcessSize = Integer.parseInt(console.readLine(PROMPT, new Date()));
        } catch (Exception e) {
            maxProcessSize = 0;
        }*/

        console.printf(LAMBDA);
        try {
            lambda = Integer.parseInt(console.readLine(PROMPT, new Date()));
        } catch (Exception e) {
            lambda = 0;
        }

        console.printf(ALPHA);
        try {
            alpha = Double.parseDouble(console.readLine(PROMPT, new Date()));

            while ((alpha < 0.0) || (alpha > 1)) {
                console.printf(ALPHA);
                alpha = Double.parseDouble(console.readLine(PROMPT, new Date()));
            }
        } catch (Exception e) {
            alpha = 0.1;
        }

        console.printf(NUM_PRINTERS);
        try {
            numPrinters = Integer.parseInt(console.readLine(PROMPT, new Date()));
        } catch (Exception e) {
            numPrinters = 1;
        }

        for (int i = 1; i <= numPrinters; i++) {
            allowedCommands.add("p" + i);//interupt
            allowedCommands.add("P" + i);//notification of completion of task
            printerQueues.add(new Vector<PCB>(1));
        }

        console.printf(NUM_DISKS);
        try {
            numDisks = Integer.parseInt(console.readLine(PROMPT, new Date()));
        } catch (Exception e) {
            numDisks = 1;
        }

        for (int i = 1; i <= numDisks; i++) {
            allowedCommands.add("d" + i);
            allowedCommands.add("D" + i);
            diskQueues.add(new Vector<PCB>(1));
        }

        console.printf(NUM_CDROMS);
        try {
            numCdroms = Integer.parseInt(console.readLine(PROMPT, new Date()));
        } catch (Exception e) {
            numCdroms = 1;
        }

        for (int i = 1; i <= numCdroms; i++) {
            allowedCommands.add("c" + i);
            allowedCommands.add("C" + i);
            cdromQueues.add(new Vector<PCB>(1));
        }

        console.printf(SYSGEN_FINISHED);

        finishedSysgen = true;

        return finishedSysgen;
    }

    private static void execRunning(Console console) {
        while (running) {
            console.printf(INPUT_NEEDED, "Next Command");
            String input = console.readLine(PROMPT, new Date());
            Scanner scanner = new Scanner(input);

            if (scanner.hasNext()) {
                final String commandName = scanner.next();
                if (allowedCommands.contains(commandName)) {
                    char commandPrefix = commandName.charAt(0);
                    int commandSuffix = (commandName.length() > 1) ? Integer.parseInt(commandName.substring(1, 2)) : -1;

                    switch (commandPrefix) {
                        case 'A':
                            //process has arrived
                            int interactiveProcessSize = 0;
                            //ask for the size of the process
                            console.printf(PROCESS_SIZE,sizeOfMemory);
                            try {
                                interactiveProcessSize = Integer.parseInt(console.readLine(PROMPT, new Date()));
                                if(interactiveProcessSize > sizeOfMemory){
                                    console.printf(PROCESS_TOO_BIG);
                                }
                                while ((interactiveProcessSize <= 0) || (interactiveProcessSize > sizeOfMemory)) {
                                    console.printf(PROCESS_SIZE,sizeOfMemory);
                                    interactiveProcessSize = Integer.parseInt(console.readLine(PROMPT, new Date()));
                                    if(interactiveProcessSize > sizeOfMemory){
                                        console.printf(PROCESS_TOO_BIG);
                                    }
                                }
                            } catch (Exception e) {
                                interactiveProcessSize = 2;
                            }


                            //preempt current process
                            if (currentPCB != null) {
                                suspendCurrentAndQueue(console, false);
                            }

                            createNewInteractiveProcess(console, interactiveProcessSize);
                            break;
                        case 'B':
                            //process has arrived

                            int batchProcessSize = 0;
                            //ask for the size of the process
                            console.printf(PROCESS_SIZE,sizeOfMemory);
                            try {
                                batchProcessSize = Integer.parseInt(console.readLine(PROMPT, new Date()));
                                if(batchProcessSize > sizeOfMemory){
                                    console.printf(PROCESS_TOO_BIG);
                                }
                                while ((batchProcessSize <= 0) || (batchProcessSize > sizeOfMemory)) {
                                    console.printf(PROCESS_SIZE,sizeOfMemory);
                                    batchProcessSize = Integer.parseInt(console.readLine(PROMPT, new Date()));
                                    if(batchProcessSize > sizeOfMemory){
                                        console.printf(PROCESS_TOO_BIG);
                                    }
                                }
                            } catch (Exception e) {
                                batchProcessSize = 2;
                            }

                            //preempt current process
                            if (currentPCB != null) {
                                suspendCurrentAndQueue(console, false);
                            }
                            createNewBatchProcess(console, batchProcessSize);
                            break;
                        case 't':
                            //terminate current process

                            //store burst length in historical burst length array in PCB
                            //echo the PID, Total accumulated time and BurstLengthAverage of that process
                            //the current process has terminated
                            
                            tallyCurrentProcess(console);//save this last burst time etc.

                            printCurrentPcbInfo(console);
                            terminateCurrentProcess();
                            break;
                        case 'K':
                            //kill process with pid x
                            //preempt current process
                            if (currentPCB != null) {
                                suspendCurrentAndQueue(console, false);
                            }
                            //store burst length in historical burst length array in PCB
                            //echo the PID, Total accumulated time and BurstLengthAverage of that process
                            //kill process with the commandSuffix passed
                            if (commandSuffix > 0) {
                                try {
                                    printPcbInfoWithPid(commandSuffix, console);
                                    terminateProcessWithPid(commandSuffix);
                                } catch (Exception e) {
                                    console.printf(COMMAND_ERROR, commandName, e.getMessage());
                                    break;
                                }
                            } else {
                                console.printf(COMMAND_ERROR, commandName, "Invalid Pid");
                            }
                            break;
                        case 'S':
                            // deal with the snapshot

                            //preempt current process
                            if (currentPCB != null) {
                                suspendCurrentAndQueue(console, false);
                            }

                            showSnapshot(console);
                            break;
                        case 'p':
                            //send current task to the printer queue

                            tallyCurrentProcess(console);//save this last burst time etc.

                            //ask for starting address of where to print from
                            //lookup the starting addrss in the current PCB's page table
                            //display the physical address that is stored in the page table

                            if (commandSuffix > 0) {
                                Vector<PCB> printerQueue = printerQueues.elementAt(commandSuffix - 1);
                                try {
                                    enqueueCurrentTaskToPrinter(console, printerQueue);
                                } catch (Exception e) {
                                    console.printf(COMMAND_ERROR, commandName, e.getMessage());
                                    break;
                                }
                            } else {
                                console.printf(COMMAND_ERROR, commandName, "Invalid printer");
                            }
                            break;
                        case 'P':
                            //end current printer task

                            //preempt current process
                            if (currentPCB != null) {
                                suspendCurrentAndQueue(console, false);
                            }

                            if (commandSuffix > 0) {
                                Vector<PCB> printerQueue = printerQueues.elementAt(commandSuffix - 1);
                                if (!printerQueue.isEmpty()) {
                                    printerTaskFinished(printerQueue);
                                } else {
                                    console.printf(COMMAND_ERROR, commandName, "Printer Queue is empty");
                                }
                            } else {
                                console.printf(COMMAND_ERROR, commandName, "Invalid printer");
                            }
                            break;
                        case 'd':
                            //send current task to the disk queue

                            tallyCurrentProcess(console);//save this last burst time etc.

                            //ask for starting address of where to print from
                            //lookup the starting addrss in the current PCB's page table
                            //display the physical address that is stored in the page table

                            if (commandSuffix > 0) {
                                Vector<PCB> diskQueue = diskQueues.elementAt(commandSuffix - 1);
                                try {
                                    enqueueCurrentTaskToDisk(console, diskQueue);
                                } catch (Exception e) {
                                    console.printf(COMMAND_ERROR, commandName, e.getMessage());
                                    break;
                                }
                            } else {
                                console.printf(COMMAND_ERROR, commandName, "Invalid disk");
                            }
                            break;
                        case 'D':
                            //deal with killing the disk task

                            //preempt current process
                            if (currentPCB != null) {
                                suspendCurrentAndQueue(console, false);
                            }

                            if (commandSuffix > 0) {
                                Vector<PCB> diskQueue = diskQueues.elementAt(commandSuffix - 1);
                                if (!diskQueue.isEmpty()) {
                                    diskTaskFinished(diskQueue);
                                } else {
                                    console.printf(COMMAND_ERROR, commandName, "Disk Queue is empty");
                                }
                            } else {
                                console.printf(COMMAND_ERROR, commandName, "Invalid disk");
                            }
                            break;
                        case 'c':
                            //send current task to the cdrom queue

                            tallyCurrentProcess(console);//save this last burst time etc.

                            //ask for starting address of where to print from
                            //lookup the starting addrss in the current PCB's page table
                            //display the physical address that is stored in the page table

                            if (commandSuffix > 0) {
                                Vector<PCB> cdromQueue = cdromQueues.elementAt(commandSuffix - 1);
                                try {
                                    enqueueCurrentTaskToCdrom(console, cdromQueue);
                                } catch (Exception e) {
                                    console.printf(COMMAND_ERROR, commandName, e.getMessage());
                                    break;
                                }
                            } else {
                                console.printf(COMMAND_ERROR, commandName, "Invalid cdrom");
                            }
                            break;
                        case 'C':
                            //deal with killing the cdrom task

                            //preempt current process
                            if (currentPCB != null) {
                                suspendCurrentAndQueue(console, false);
                            }

                            if (commandSuffix > 0) {
                                Vector<PCB> cdromQueue = cdromQueues.elementAt(commandSuffix - 1);
                                if (!cdromQueue.isEmpty()) {
                                    cdromTaskFinished(cdromQueue);
                                } else {
                                    console.printf(COMMAND_ERROR, commandName, "Cdrom Queue is empty");
                                }
                            } else {
                                console.printf(COMMAND_ERROR, commandName, "Invalid cdrom");
                            }
                            break;
                        case 'X':
                            //switch global queue priority

                            //preempt current process
                            if (currentPCB != null) {
                                suspendCurrentAndQueue(console, false);
                            }

                            if (globalQueuePriority.equalsIgnoreCase("batch")) {
                                globalQueuePriority = "interactive";
                            } else {
                                globalQueuePriority = "batch";
                            }
                            break;
                        case 'T':
                            //Timer interupt

                            //preempt current process
                            if (currentPCB != null) {
                                suspendCurrentAndQueue(console, false);
                            }
                            break;
                        case 'Z':
                            //print current pcb

                            //preempt current process
                            if (currentPCB != null) {
                                console.printf(currentPCB.toString()+"\n");
                                insertIntoBatchQueueInOrder(currentPCB);
                                currentPCB = null;
                            } else {
                                console.printf("currentPCB is empty! \n");
                            }
                            break;
                    }
                } else {
                    console.printf(UNKNOWN_COMMAND, commandName);
                }
                
                schedule(); // pick the next process
            }

            
            scanner.close();
        }
    }

    private static void insertIntoBatchJobPoolInOrder(PCB pcb) {
        //walk through the vector and find the right position for this pcb
        // this will make the smallest processes first and the largest last
        if (batchJobPool.isEmpty()) {
            batchJobPool.add(pcb);
        } else {
            int processSizeFloor = (int)Math.floor(pcb.getProcessSize());
            int insertionPoint = 0;
            ListIterator<PCB> itr = batchJobPool.listIterator();
            try {
                while (itr.next().getProcessSize() < processSizeFloor) {
                    insertionPoint++;
                }
            } catch (Exception e) {
                // this will probably happen
            }

            batchJobPool.ensureCapacity(insertionPoint + 1);
            batchJobPool.insertElementAt(pcb, insertionPoint);
        }
    }

    private static void insertIntoInteractiveJobPoolInOrder(PCB pcb) {
        //walk through the vector and find the right position for this pcb
        // this will make the smallest processes first and the largest last
        if (interactiveJobPool.isEmpty()) {
            interactiveJobPool.add(pcb);
        } else {
            int processSizeFloor = (int)Math.floor(pcb.getProcessSize());
            int insertionPoint = 0;
            ListIterator<PCB> itr = interactiveJobPool.listIterator();
            try {
                while (itr.next().getProcessSize() < processSizeFloor) {
                    insertionPoint++;
                }
            } catch (Exception e) {
                // this will probably happen
            }

            interactiveJobPool.ensureCapacity(insertionPoint + 1);
            interactiveJobPool.insertElementAt(pcb, insertionPoint);
        }
    }

    private static void putProcessIntoMemory(PCB processPCB) throws Exception{
        int processSizeInPages = calculateNumPagesfromProcessSize(processPCB.getProcessSize());
        if( processSizeInPages < numFreePages){
            int i=0;
            while(processSizeInPages > 0){
                if(frameTable.elementAt(i)==null){
                    //add the 2-tuple process pid and the location of this page within the processes page table to the frametable.
                    frameTable.set(i,new Integer[]{processPCB.getPid(),processPCB.addPageLocationToPageTable(i)});
                    processSizeInPages--;
                    numFreePages--;
                }
                i++;
            }
        } else {
            throw new Exception("Not enough pages free to load this process.");
        }
    }

    private static void removeProcessFromMemory(PCB processPCB) {
        Iterator<Integer> pageTableIterator = processPCB.getPageTable().iterator();
        while(pageTableIterator.hasNext()){
            int pageLocation = pageTableIterator.next();
            frameTable.setElementAt(null, pageLocation);
            numFreePages++;
        }
        //not sure if we want to delete the existing pageTable or keep it yet
        //actually the pageTable will be garbage collected along with the pcb
        //when all references to the pcb are removed.
        //Vector<Integer> pageTable = new Vector<Integer>(1);
        //processPCB.setPageTable(pageTable);
    }

    private static int calculateNumPagesfromProcessSize(int processSize){
        return (int)Math.ceil((double)processSize/(double)sizeOfPage);
    }

    // the following is an implementation of firstfit.
    private static int findFirstLargestHole() {
        int largestHoleSize = 0;
        int currentHoleSize = 0;
        int largestHoleStartingAddress = 0;
        int currentHoleStartingAddres = 0;
        for(int i=0; i<sizeOfMemory; i++) {
            if(frameTable.elementAt(i) == null) {
                currentHoleSize += 1;
            } else {
                if(currentHoleSize > largestHoleSize){
                    largestHoleStartingAddress = currentHoleStartingAddres;
                    largestHoleSize = currentHoleSize;
                    largestHoleStartingAddress = currentHoleStartingAddres;
                }
                currentHoleStartingAddres = i+1;
            }
        }
        return largestHoleStartingAddress;
    }

    private static int getNextPid() {
        pidCounter = pidCounter + 1; //the first user process can at best be 1
        return pidCounter;
    }

    private static void printCurrentPcbInfo(Console console) {
        printPcbInfoWithPid(currentPCB.getPid(), console);
    }

    private static void terminateCurrentProcess() {
        //the current process finished
        terminateProcessWithPid(currentPCB.getPid());
        //currentPCB = null; //signal garbage collection of object
    }

    private static void terminateProcessWithPid(int pid) {
        //find the PCB matching that pid
        try {
            ListIterator<PCB> itr = allActivePids.listIterator();
            PCB myPCB = null;
            while (itr.hasNext()) {
                myPCB = itr.next();
                if (myPCB.getPid() == pid) {
                    break;
                }
            }

            //check to see if the pid sent was the curent pcb.
            if (currentPCB.getPid() == myPCB.getPid()) {
                currentPCB = null;
            }

            allActivePids.remove(myPCB);
            totalCpuTimeOfAllCompletedProccesses = totalCpuTimeOfAllCompletedProccesses + myPCB.getTotalCpuTime();
            if (myPCB.getProcessType().equalsIgnoreCase("batch")) {
                batchReadyQueue.remove(myPCB);
            } else {
                interactiveReadyQueue.remove(myPCB);
            }

            removeProcessFromMemory(myPCB);
        } catch (Exception e) {
            //this shouldnt happen
        }
    }

    private static void printPcbInfoWithPid(int pid, Console console) {
        //find the PCB matching that pid
        //PCB myPCB = allActivePids.get(pid);
        ListIterator<PCB> itr = allActivePids.listIterator();
        PCB myPCB = null;
        while (itr.hasNext()) {
            myPCB = itr.next();
            if (myPCB.getPid() == pid) {
                break;
            }
        }
        console.printf(myPCB.toString());
        console.printf("\n");
        //console.printf("PID: %d | Total CPU time: %f | Average burst length: %f $n", myPCB.getPid(), myPCB.getTotalCpuTime(), myPCB.getTauAverage());
    }

    /* fifo is inherently round robin
    private static PCB getNextInteractivePCB() {
    if(interactiveRoundRobinCounter > interactiveReadyQueue.size()) {
    interactiveRoundRobinCounter = 1;
    return interactiveReadyQueue.elementAt(0);
    } else {
    interactiveRoundRobinCounter++;
    return interactiveReadyQueue.elementAt(interactiveRoundRobinCounter-1);
    }
    }*/
    private static void insertIntoBatchQueueInOrder(PCB pcb) {
        //this variable will determine where this pcb is inserted into the batch
        //queue
        double tauForPlacement = 0.0;

        //if the pcb is brand new then we don't want to re-estimate it's tau
        if (allActivePids.contains(pcb)) {
            //determine if we are finished with our burst or if we were preempted
            //if current burst is greater than 0.0 then we know we still have time
            //left in this burst
            if (pcb.getCurrentBurst() > 0.0) {
                double tauLeftOver = pcb.getTauLeft();
                tauForPlacement = tauLeftOver;
            } else {
                double tauNext = alpha * (double) pcb.getTau() + (1 - alpha) * pcb.getLastBurst();
                pcb.setTau(tauNext);
                tauForPlacement = tauNext;
            }
        } else {
            tauForPlacement = pcb.getTau(); //this should be 5ms at this point
        }

        //walk through the vector and find the right position for this pcb
        if (batchReadyQueue.isEmpty()) {
            batchReadyQueue.add(pcb);
        } else {
            //int tauCeil = (int)Math.ceil(tauNext);
            //int tauFloor = (int)Math.floor(tauForPlacement);
            int tauFloor = 0;
            ListIterator<PCB> itr = batchReadyQueue.listIterator();
            try {
                while (itr.next().getTauLeft() < tauForPlacement) {
                    tauFloor++;
                }
            } catch (Exception e) {
                // this will probably happen
            }

            batchReadyQueue.ensureCapacity(tauFloor + 1);
            batchReadyQueue.insertElementAt(pcb, tauFloor);
        }
    }

    private static void createNewInteractiveProcess(Console console, int processSize) {
        PCB pcb = new PCB("interactive", getNextPid(), 0.0, processSize);
        
        //check to see if the process can fit right now
        if(calculateNumPagesfromProcessSize(processSize) < numFreePages){

            //mark the memory as taken by this process (frame table)
            //fill this processes page table with the memory addresses it just got.
            try{
                putProcessIntoMemory(pcb);
            } catch(Exception e){
                //this should never happen
            }
            interactiveReadyQueue.add(pcb); //push new pcb onto tail of interactiveReadyQueue
            allActivePids.add(pcb);//also add it to the global list of active pcbs

            //freeMemory = freeMemory - processSize;

            //interactiveReadyQueue.add(currentPCB);
            //schedule();
        } else {
            jobPool.add(pcb);
            insertIntoInteractiveJobPoolInOrder(pcb);
        }
    }

    private static void createNewBatchProcess(Console console, int processSize) {
        PCB pcb = new PCB("batch", getNextPid(), 5.0, processSize);
        
        //check to see if the process can fit right now
        if(calculateNumPagesfromProcessSize(processSize) < numFreePages){

            try{
                putProcessIntoMemory(pcb);
            } catch(Exception e){
                //this should never happen
            }
            //need to insert into queue at propper location based on tau approximation
            insertIntoBatchQueueInOrder(pcb);
            if(!allActivePids.contains(pcb)){
                allActivePids.add(pcb);//also add it to the global list of active pcbs
            }
            //freeMemory = freeMemory - processSize;

            //schedule();

        } else {
            jobPool.add(pcb);
            insertIntoBatchJobPoolInOrder(pcb);;
        }
    }

    private static void tallyCurrentProcess(Console console) {
        console.printf(TIME_SPENT_ON_CPU);
        try {
            double lastPartialBurst = Double.parseDouble(console.readLine(PROMPT, new Date()));

            if (currentPCB.getProcessType().equalsIgnoreCase("batch")) {
                //should only be doing this if the process was finished with its burst
                currentPCB.setLastBurst(currentPCB.getCurrentBurst() + lastPartialBurst);
                currentPCB.setCurrentBurst(0.0);

                double tauNext = alpha * (double) currentPCB.getTau() + (1 - alpha) * currentPCB.getLastBurst();
                currentPCB.setTau(tauNext);
            }

        } catch (Exception e) {
            console.printf("Wrong format for time. Must be double precision of form 0.0. Please try again.\n");
            
            tallyCurrentProcess(console);
        }
    }

    private static void suspendCurrentAndQueue(Console console, boolean burstFinished) {
        console.printf(TIME_SPENT_ON_CPU);
        try {
            double lastPartialBurst = Double.parseDouble(console.readLine(PROMPT, new Date()));

            if (currentPCB.getProcessType().equalsIgnoreCase("batch")) {
                if(burstFinished) {
                    //should only be doing this if the process was finished with its burst
                    currentPCB.setLastBurst(currentPCB.getCurrentBurst() + lastPartialBurst);
                    currentPCB.setCurrentBurst(0.0);
                } else {
                    //we just want to keep track of the last "partial burst" even though it wasnt really a burst
                    //we want to use it to know how much time we think we have left (tauLeftOver - partialBurst)
                    currentPCB.setCurrentBurst(currentPCB.getCurrentBurst() + lastPartialBurst);
                }
                
                //need to insert into queue at propper location based on tau approximation
                insertIntoBatchQueueInOrder(currentPCB);
            } else {
                //add most recent partial burst to the current running burst
                currentPCB.setCurrentBurst(currentPCB.getCurrentBurst() + lastPartialBurst);
                if(currentPCB.getCurrentBurst() >= lambda) {
                    //if this process has finished it's roundrobin share move to the end
                    //of the interactive queue
                    //i assume this means the tail end but maybe its the head?
                    interactiveReadyQueue.add(currentPCB);
                    currentPCB.setCurrentBurst(0.0);
                } else {
                    //this process still has a little time left in it's alotment of the 
                    //roundrobin's timeslice so move back to the begining of the queue.
                    interactiveReadyQueue.insertElementAt(currentPCB, 0);
                }
            }
            currentPCB = null;

        } catch (Exception e) {
            console.printf("Wrong format for time. Must be double precision of form 0.0. Please try again.\n");
            suspendCurrentAndQueue(console, burstFinished);
        }
    }

    private static void schedule() {
        if (globalQueuePriority.equalsIgnoreCase("batch")) {
            if(numFreePages > 0 && !jobPool.isEmpty()){
                Iterator<PCB> batchJobPoolIterator = batchJobPool.iterator();
                while(batchJobPoolIterator.hasNext() && numFreePages > 0){
                    PCB batchPCB = batchJobPoolIterator.next();
                    if(calculateNumPagesfromProcessSize(batchPCB.getProcessSize()) < numFreePages){
                        try{
                            putProcessIntoMemory(batchPCB);
                            jobPool.remove(batchPCB);
                            batchJobPool.remove(batchPCB);
                            insertIntoBatchQueueInOrder(batchPCB);
                            if(!allActivePids.contains(batchPCB)){
                                allActivePids.add(batchPCB);//also add it to the global list of active pcbs
                            }
                        } catch(Exception e){
                            //this should never happen
                        }
                    }
                }

                Iterator<PCB> interactiveJobPoolIterator = interactiveJobPool.iterator();
                while(interactiveJobPoolIterator.hasNext() && numFreePages > 0){
                    PCB interactivePCB = interactiveJobPoolIterator.next();
                    if(calculateNumPagesfromProcessSize(interactivePCB.getProcessSize()) < numFreePages){
                        try{
                            putProcessIntoMemory(interactivePCB);
                            jobPool.remove(interactivePCB);
                            interactiveJobPool.remove(interactivePCB);
                            interactiveReadyQueue.add(interactivePCB);
                            allActivePids.add(interactivePCB);
                        } catch(Exception e){
                            //this should never happen
                        }
                    }
                }
            }

            if (!batchReadyQueue.isEmpty()) {
                currentPCB = batchReadyQueue.remove(0);
            } else {
                if (!interactiveReadyQueue.isEmpty()) {
                    currentPCB = interactiveReadyQueue.remove(0);
                } else {
                    currentPCB = null;
                }
            }
        } else {
            if(numFreePages > 0 && !jobPool.isEmpty()){
                Iterator<PCB> interactiveJobPoolIterator = interactiveJobPool.iterator();
                while(interactiveJobPoolIterator.hasNext() && numFreePages > 0){
                    PCB interactivePCB = interactiveJobPoolIterator.next();
                    if(calculateNumPagesfromProcessSize(interactivePCB.getProcessSize()) < numFreePages){
                        try{
                            putProcessIntoMemory(interactivePCB);
                            jobPool.remove(interactivePCB);
                            interactiveJobPool.remove(interactivePCB);
                            interactiveReadyQueue.add(interactivePCB);
                            allActivePids.add(interactivePCB);
                        } catch(Exception e){
                            //this should never happen
                        }
                    }
                }
                
                Iterator<PCB> batchJobPoolIterator = batchJobPool.iterator();
                while(batchJobPoolIterator.hasNext() && numFreePages > 0){
                    PCB batchPCB = batchJobPoolIterator.next();
                    if(calculateNumPagesfromProcessSize(batchPCB.getProcessSize()) < numFreePages){
                        try{
                            putProcessIntoMemory(batchPCB);
                            jobPool.remove(batchPCB);
                            batchJobPool.remove(batchPCB);
                            insertIntoBatchQueueInOrder(batchPCB);
                            if(!allActivePids.contains(batchPCB)){
                                allActivePids.add(batchPCB);//also add it to the global list of active pcbs
                            }
                        } catch(Exception e){
                            //this should never happen
                        }
                    }
                }
            }
            if (!interactiveReadyQueue.isEmpty()) {
                currentPCB = interactiveReadyQueue.remove(0);
            } else {
                if (!batchReadyQueue.isEmpty()) {
                    currentPCB = batchReadyQueue.remove(0);
                } else {
                    currentPCB = null;
                }
            }
        }
    }

    private static void printerTaskFinished(Vector<PCB> printerQueue) {
        PCB currentPrinterPCB = printerQueue.remove(0);
        if (currentPrinterPCB.getProcessType().equalsIgnoreCase("batch")) {
            insertIntoBatchQueueInOrder(currentPrinterPCB);
        } else {
            interactiveReadyQueue.add(currentPrinterPCB);
        }
    }

    private static void diskTaskFinished(Vector<PCB> diskQueue) {
        PCB currentDiskPCB = diskQueue.remove(0);
        if (currentDiskPCB.getProcessType().equalsIgnoreCase("batch")) {
            insertIntoBatchQueueInOrder(currentDiskPCB);
        } else {
            interactiveReadyQueue.add(currentDiskPCB);
        }
    }

    private static void cdromTaskFinished(Vector<PCB> cdromQueue) {
        PCB currentCdromPCB = cdromQueue.remove(0);
        if (currentCdromPCB.getProcessType().equalsIgnoreCase("batch")) {
            insertIntoBatchQueueInOrder(currentCdromPCB);
        } else {
            interactiveReadyQueue.add(currentCdromPCB);
        }
    }

    private static void enqueueCurrentTaskToPrinter(Console console, Vector<PCB> printerQueue) throws Exception {
        currentPCB = hydratePCB(console, currentPCB, "print");
        printerQueue.add(currentPCB);
        //schedule();
//        if (!interactiveReadyQueue.isEmpty()) {
//            currentPCB = interactiveReadyQueue.remove(0); //remove next pcb from the front of interactiveReadyQueue
//        } else {
//            currentPCB = null;
//        }
    }

    private static void enqueueCurrentTaskToDisk(Console console, Vector<PCB> diskQueue) throws Exception {
        currentPCB = hydratePCB(console, currentPCB, "disk");
        diskQueue.add(currentPCB);
        //schedule();
//        if (!interactiveReadyQueue.isEmpty()) {
//            currentPCB = interactiveReadyQueue.remove(0); //remove next pcb from the front of interactiveReadyQueue
//        } else {
//            currentPCB = null;
//        }
    }

    private static void enqueueCurrentTaskToCdrom(Console console, Vector<PCB> cdromQueue) throws Exception {
        currentPCB = hydratePCB(console, currentPCB, "cdrom");
        cdromQueue.add(currentPCB);
        //schedule();
//        if (!interactiveReadyQueue.isEmpty()) {
//            currentPCB = interactiveReadyQueue.remove(0); //remove next pcb from the front of interactiveReadyQueue
//        } else {
//            currentPCB = null;
//        }
    }

    private static PCB hydratePCB(Console console, PCB pcb, String taskType) throws Exception {
        console.printf(INPUT_NEEDED, "Full path to file");
        String fileName = console.readLine(PROMPT, new Date());
        if (fileName.length() < 2) { // imediately toss the exception all files are at least "/something"
            throw new Exception("Invalid file path. Filename should be at least 2 characters long.");
        }
//        File f = new File(fileName);
//        if (!f.exists()) {
//            throw new Exception("File does not exist");
//        }
//        f = null;

        console.printf(INPUT_NEEDED, "Starting Location");
        int startingLocation;
        try {
            startingLocation = Integer.parseInt(console.readLine(PROMPT, new Date()));
        } catch (Exception e) {
            throw new Exception("Invalid memory location");
        }

        char action;
        if (!taskType.equalsIgnoreCase("print")) {
            console.printf(INPUT_NEEDED, "Action");
            try {
                action = console.readLine(PROMPT, new Date()).charAt(0);
                if (action != 'r' && action != 'w') {
                    throw new Exception();
                }
            } catch (Exception e) {
                throw new Exception("Invalid Action");
            }
        } else {
            action = 'w';
        }

        int fileLength;
        if (action == 'w') {
            console.printf(INPUT_NEEDED, "File Length");
            try {
                fileLength = Integer.parseInt(console.readLine(PROMPT, new Date()));
            } catch (Exception e) {
                throw new Exception("Invalid file length");
            }
        } else {
            fileLength = 0;
        }
        pcb.setFileName(fileName);
        pcb.setStartingLocation(startingLocation);
        pcb.setAction(action);
        pcb.setFileLength(fileLength);

        return pcb;
    }

    private static void printPCBQueue(ListIterator<Vector<PCB>> queuesQueue, Console console){
        //int columns = 0;
        int lines = 2;
        int queueCount = 0;
        while(queuesQueue.hasNext()){
            queueCount++;
            Vector<PCB> myPCBVector = queuesQueue.next();
            ListIterator<PCB> myPCBItr = myPCBVector.listIterator();

            while (myPCBItr.hasNext()) {
                PCB myPCB = myPCBItr.next();
                Vector<Integer> myPageTable = myPCB.getPageTable();
                myPageTable.trimToSize();
                lines = lines + myPageTable.size();
                String pcbString = myPCB.toString();
                console.printf(pcbString + "\n");
                if (lines >= 20) {
                    console.printf("\nPress enter/return to show more");
                    console.readLine(PROMPT, new Date());
                }
            }
            
            if (lines >= 20) {
                console.printf("\nPress enter/return to show more");
                console.readLine(PROMPT, new Date());
            }
            lines = lines + 3;
        }
    }

    private static void showSnapshot(Console console) {
        console.printf(INPUT_NEEDED, "Queue type");
        char queueType = console.readLine(PROMPT, new Date()).charAt(0);
        if (queueType != 'r' && queueType != 'p' && queueType != 'd' && queueType != 'c' && queueType != 'm') {
            console.printf("Invalid queue type. Must be one of [r,p,d,c,m] please try again.");
            showSnapshot(console);
        }

        int columns = 0;
        int lines = 2;

        switch (queueType) {
            case 'm':
                //show frametable
                Vector<Integer[]> myFrameTable = (Vector<Integer[]>)frameTable.clone();
                myFrameTable.trimToSize();
                ListIterator<Integer[]> frameTableItr = myFrameTable.listIterator();
                console.printf("\nm:\n");
                while (frameTableItr.hasNext()) {
                    //String pid = Integer.toString(readyQ.next().getPid());
                    Integer[] myFrame = frameTableItr.next();
                    if(myFrame != null){
                        String frameString = myFrame[0].toString() + '|' + myFrame[1].toString();
                        console.printf("\n");
                        console.printf('|' + frameString + '|');
                        lines = lines + 1;

                        if (lines >= 20) {
                            console.printf("\nPress enter/return to show more");
                            console.readLine(PROMPT, new Date());
                        }
                    }
                }
                console.printf("\n");
                break;
            case 'r':
                //show ready queue
                Vector<PCB> myBatchVector = (Vector<PCB>)batchReadyQueue.clone();
                Vector<PCB> myInteractiveVector = (Vector<PCB>)interactiveReadyQueue.clone();
                myBatchVector.addAll(myInteractiveVector);//combine both queues

                Vector<Vector<PCB>> myVector = new Vector(0);
                myVector.add(myBatchVector);

                ListIterator<Vector<PCB>> readyQ = myVector.listIterator();
                console.printf("\nr:\n");
                printPCBQueue(readyQ,console);

                /*while (readyQ.hasNext()) {
                    //String pid = Integer.toString(readyQ.next().getPid());
                    String pcbString = readyQ.next().toString();

                    if (columns + pcbString.length() + 1 < 80) {
                        console.printf(pcbString + "|");
                        columns = columns + pcbString.length() + 1;
                    } else {
                        console.printf("\n");
                        console.printf(pcbString + '|');
                        columns = pcbString.length() + 1;
                        lines = lines + 1;
                    }

                    if (lines >= 20) {
                        console.printf("\nPress enter/return to show more");
                        console.readLine(PROMPT, new Date());
                    }
                }*/
                console.printf("\n");
                if(totalCpuTimeOfAllCompletedProccesses > 0.0) {
                    console.printf("Total CPU time of all completed processes: %f \n", totalCpuTimeOfAllCompletedProccesses);
                } else {
                    console.printf("Total CPU time of all completed processes: 0.0 \n");
                }
                console.printf("Finished Printing Ready Queue. Press enter/return to continue.\n");
                console.readLine(PROMPT, new Date());
                break;
            case 'p':
                //show printer queues
                //int printerQueueCount = 0;
                ListIterator<Vector<PCB>> printersQ = printerQueues.listIterator();
                printPCBQueue(printersQ,console);
                /*while (printersQ.hasNext()) {
                    printerQueueCount++;
                    console.printf("\np%s:\n", printerQueueCount);
                    Vector<PCB> printerQueue = printersQ.next();
                    ListIterator<PCB> printerQ = printerQueue.listIterator();
                    while (printerQ.hasNext()) {
                        //String pid = Integer.toString(printerQ.next().getPid());
                        String pcbString = printerQ.next().toString();
                        if (columns + pcbString.length() + 1 < 80) {
                            console.printf(pcbString + "|");
                            columns = columns + pcbString.length() + 1;
                        } else {
                            console.printf("\n");
                            console.printf(pcbString + '|');
                            columns = pcbString.length() + 1;
                            lines = lines + 1;
                        }

                        if (lines >= 20) {
                            console.printf("\nPress enter/return to show more");
                            console.readLine(PROMPT, new Date());
                        }
                    }
                    if (lines >= 20) {
                        console.printf("\nPress enter/return to show more");
                        console.readLine(PROMPT, new Date());
                    }
                    lines = lines + 3;
                }*/

                console.printf("\n");
                if(totalCpuTimeOfAllCompletedProccesses > 0.0) {
                    console.printf("Total CPU time of all completed processes: %f \n", totalCpuTimeOfAllCompletedProccesses);
                } else {
                    console.printf("Total CPU time of all completed processes: 0.0 \n");
                }
                console.printf("Finished Printing printer queues. Press enter/return to continue.\n");
                console.readLine(PROMPT, new Date());
                break;
            case 'd':
                //show disk queues
                //int diskQueueCount = 0;
                ListIterator<Vector<PCB>> disksQ = diskQueues.listIterator();
                printPCBQueue(disksQ,console);
                /*while (disksQ.hasNext()) {
                    diskQueueCount++;
                    console.printf("\nd%s:\n", diskQueueCount);
                    Vector<PCB> diskQueue = disksQ.next();
                    ListIterator<PCB> diskQ = diskQueue.listIterator();
                    while (diskQ.hasNext()) {
                        //String pid = Integer.toString(diskQ.next().getPid());
                        String pcbString = diskQ.next().toString();
                        if (columns + pcbString.length() + 1 < 80) {
                            console.printf(pcbString + "|");
                            columns = columns + pcbString.length() + 1;
                        } else {
                            console.printf("\n");
                            console.printf(pcbString + '|');
                            columns = pcbString.length() + 1;
                            lines = lines + 1;
                        }

                        if (lines >= 20) {
                            console.printf("\nPress enter/return to show more");
                            console.readLine(PROMPT, new Date());
                        }
                    }
                    if (lines >= 20) {
                        console.printf("\nPress enter/return to show more");
                        console.readLine(PROMPT, new Date());
                    }
                    lines = lines + 3;
                }*/
                console.printf("\n");
                if(totalCpuTimeOfAllCompletedProccesses > 0.0) {
                    console.printf("Total CPU time of all completed processes: %f \n", totalCpuTimeOfAllCompletedProccesses);
                } else {
                    console.printf("Total CPU time of all completed processes: 0.0 \n");
                }
                console.printf("Finished displaying the disk queues. Press enter/return to continue.\n");
                console.readLine(PROMPT, new Date());
                break;
            case 'c':
                //show cdrom queues
                //int cdromQueueCount = 0;
                ListIterator<Vector<PCB>> cdromsQ = cdromQueues.listIterator();
                printPCBQueue(cdromsQ,console);
                /*while (cdromsQ.hasNext()) {
                    cdromQueueCount++;
                    console.printf("\nc%s:\n", cdromQueueCount);
                    Vector<PCB> cdromQueue = cdromsQ.next();
                    ListIterator<PCB> cdromQ = cdromQueue.listIterator();
                    while (cdromQ.hasNext()) {
                        //String pid = Integer.toString(cdromQ.next().getPid());
                        String pcbString = cdromQ.next().toString();
                        if (columns + pcbString.length() + 1 < 80) {
                            console.printf(pcbString + "|");
                            columns = columns + pcbString.length() + 1;
                        } else {
                            console.printf("\n");
                            console.printf(pcbString + '|');
                            columns = pcbString.length() + 1;
                            lines = lines + 1;
                        }

                        if (lines >= 20) {
                            console.printf("\nPress enter/return to show more");
                            console.readLine(PROMPT, new Date());
                        }
                    }
                    if (lines >= 20) {
                        console.printf("\nPress enter/return to show more");
                        console.readLine(PROMPT, new Date());
                    }
                    lines = lines + 3;
                }*/
                console.printf("\n");
                if(totalCpuTimeOfAllCompletedProccesses > 0.0) {
                    console.printf("Total CPU time of all completed processes: %f \n", totalCpuTimeOfAllCompletedProccesses);
                } else {
                    console.printf("Total CPU time of all completed processes: 0.0 \n");
                }
                console.printf("Finished displaying cdrom queues. Press enter/return to continue.\n");
                console.readLine(PROMPT, new Date());
                break;
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Console console = System.console();
        if (console != null) {
            if (execSysgen(console)) {
                running = true;
                execRunning(console);
            }
        }
    }
}
