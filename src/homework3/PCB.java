package homework3;

import java.util.*;

public class PCB {

    private String processType = "";
    private int pid;
    private double tau = 0.0; //in milliseconds
    private double totalTau = 0.0; //in milliseconds
    private double lastBurst = 0.0; //in milliseconds
    private double currentBurst = 0.0; //in milliseconds
    private int numTaus = 0;
//    private double lastPartialBurst = 0.0; //in milliseconds
    private double totalCpuTime = 0.0;
    private String fileName;
    private char action;
    private int startingLocation;
    private int fileLength;
    private int processSize; // size in words but must be multiple of page size
                             // must also not be larger that total memory size

    private Vector pageTable = new Vector<Integer>(0); // this will hold
                                    //all of this processes logical to physical
                                    //memory mappings.

    public PCB(String processType, int pid, double tau, int processSize) {
        this.processType = processType;
        this.pid = pid;
        this.tau = tau;
        this.processSize = processSize;
    }

    public String getProcessType(){
        return this.processType;
    }

    public int getPid() {
        return this.pid;
    }

    public double getTau() {
        return this.tau;
    }

    public void setTau(double tau) {
        this.tau = tau;
        this.numTaus++;
        this.setTotalTau(this.getTotalTau()+tau);
    }

    public double getTotalTau() {
        return this.totalTau;
    }

    public void setTotalTau(double tau) {
        this.totalTau = this.totalTau+tau;
    }

    public double getTauLeft() {
        double tauLeftOver = this.getTau() - this.getCurrentBurst();
        if (tauLeftOver < 0.0) {
            tauLeftOver = 0.0;
        }
        return tauLeftOver;
    }

    public double getTauAverage() {
        if(numTaus > 0){
            return this.getTotalTau()/numTaus;
        } else {
            return 0.0;
        }
    }

    public double getLastBurst() {
        return this.lastBurst;
    }

    public void setLastBurst(double lastBurst) {
        this.lastBurst = lastBurst;
        
        //add this burst to the total cpu time used by this PCB
        this.setTotalCpuTime(this.getTotalCpuTime()+lastBurst);
    }

    public double getCurrentBurst() {
        return this.currentBurst;
    }

    public void setCurrentBurst(double currentBurst) {
        this.currentBurst = currentBurst;
    }

    public double getTotalCpuTime() {
        return this.totalCpuTime;
    }

    public void setTotalCpuTime(double totalCpuTime){
        this.totalCpuTime = totalCpuTime;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setAction(char action) {
        this.action = action;
    }

    public char getAction() {
        return this.action;
    }

    public void setStartingLocation(int startingLocation) {
        this.startingLocation = startingLocation;
    }

    public int getStartingLocation() {
        return this.startingLocation;
    }

    public void setFileLength(int fileLength) {
        this.fileLength = fileLength;
    }

    public int getFileLength() {
        return this.fileLength;
    }

    public int getProcessSize() {
        return this.processSize;
    }

    //returns the location in the page table that this pageLocation was added to
    public int addPageLocationToPageTable(int pageLocation){
        this.pageTable.add(pageLocation);
        return this.pageTable.indexOf(pageLocation);
    }

    public Vector<Integer> getPageTable(){
        return this.pageTable;
    }

    public void setPageTable(Vector<Integer> pageTable){
        this.pageTable = pageTable;
    }

    public String printPageTable(){
        ListIterator<Integer> pageTableItr = this.pageTable.listIterator();
        String output = "";
        int i = 0;
        while(pageTableItr.hasNext()){
            output = output + "|" + i + "|" + pageTableItr.next().toString() + "|\n";
            i++;
        }
        return output;
    }

    public String toString(){
            return  "pid: " + this.pid + " fn:" + this.fileName + " a:" +
                    this.action + " sl:" + this.startingLocation + " fl:" +
                    this.fileLength + " T CPU:" + this.getTotalCpuTime() +
                    " Av brst:" + this.getTauAverage() + "\n" +
                    this.printPageTable();

                    /*+ " type: " + this.getProcessType() +
                    " tau: " + this.getTau() + " totalTau: " + this.getTotalTau() +
                    " current burst: " + this.getCurrentBurst();*/
    }
}
