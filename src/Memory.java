import java.io.*;
import java.util.*;

/**
 * This is a class that contains Zone class.
 * This class contains such methods: firstFit, nextFit, bestFit, allocation, recycle, increaseAddress,
 * decreaseAddress, showZones, getProcess, writeCsv, statistics.
 */

public class Memory {
    private int size; // Define the size of the whole memory.
    private LinkedList<Zone> zones; // We use a linked list to simulate the address.
    private int pointer; // We also hava a pointer.

    /**
     * This is a zone. We use this to store every memory process.
     */
    class Zone {
        private int head; // Head of the zone.
        private int size; // Size of the zone.
        private boolean isFree; // If it is free.

        public Zone(int head,int size) {
            this.head = head;
            this.size = size;
            this.isFree = true;
        }
    }

    public Memory() {
        this.size = 1024;
        this.pointer = 0;
        this.zones = new LinkedList<>();
        zones.add(new Zone(0,size));
    }

    public Memory(int size) {
        this.size = size;
        this.pointer = 0;
        this.zones = new LinkedList<>();
        zones.add(new Zone(0,size));
    }

   
    public int firstFit(MemoryProcess memoryProcess){
        int size = memoryProcess.getSize();
        for (pointer = 0; pointer < zones.size(); pointer++){
            Zone tmp = zones.get(pointer);
            // If the condition is satisfied. Store it.
            if (tmp.isFree && (tmp.size > size)){
            	//allocate the new process in the wait queue and get its new address
                int newPointer = allocate(size, pointer, tmp);
                // Put it into running process list.
                FCFS.runningProcess.add(memoryProcess);
                return newPointer;
            }
        }
        // Condition isn't satisfied. Put it into waiting queue.
        FCFS.waitProcess.add(memoryProcess);
        System.out.println("Out of memory, delay!");
        return -1;
    }

   
    public int nextFit(MemoryProcess memoryProcess){
        int size = memoryProcess.getSize();
        int len = zones.size();
        // In case beyond the index of the size.
        if (pointer >= len){
            pointer = 0;
        }
        Zone tmp = zones.get(pointer);
        // If the head of the zones is free, store it.
        if (tmp.isFree && (tmp.size > size)){
            allocate(size, pointer, tmp);
            pointer++;
            FCFS.runningProcess.add(memoryProcess);
            return (pointer-1);
        }
        // Traverse the queue from last position to the end of the zones.
        for (; pointer < len ; pointer++){
            tmp = zones.get(pointer);
            // Find the zone, and it's big enough.
            if (tmp.isFree && (tmp.size > size)){
                allocate(size, pointer, tmp);
                FCFS.runningProcess.add(memoryProcess);
                return pointer;
            }
        }
        int prePointer = pointer;
        // Traverse from the head of the zones to last position.
        for (pointer = 0; pointer < prePointer; pointer++){
            tmp = zones.get(pointer);
            //Find the zone, and it's big enough.
            if (tmp.isFree && (tmp.size > size)) {
                allocate(size, pointer, tmp);
                FCFS.runningProcess.add(memoryProcess);
                return pointer;
            }
        }
        // Condition isn't satisfied. Put it into waiting queue.
        FCFS.waitProcess.add(memoryProcess);
        System.out.println("Out of memory, delay!");
        return -1;
    }

    
    public int bestFit(MemoryProcess memoryProcess){
        int size = memoryProcess.getSize();
        int flag = -1;
        int min = this.size;
        // Traverse the zones, find the most suitable one.
        for (pointer = 0; pointer < zones.size(); pointer++){
           Zone tmp = zones.get(pointer);
            if (tmp.isFree && (tmp.size > size)){
                if (min > tmp.size - size){
                    min = tmp.size - size;
                    flag = pointer;
                }
            }
        }
        if (flag == -1){
            // Condition isn't satisfied. Put it into waiting.
            FCFS.waitProcess.add(memoryProcess);
            System.out.println("Out of memory, delay!");
            return -1;
        }else {
            FCFS.runningProcess.add(memoryProcess);
            allocate(size, flag, zones.get(flag));
            // Return the position and assign it to process as an address.
            return flag;
        }
    }

   
    public int allocate(int size, int location, Zone tmp){
        // Redistribute zone.
       Zone split = new Zone(tmp.head + size, tmp.size - size);
        zones.add(location + 1, split);
        tmp.size = size;
        tmp.isFree = false;
        System.out.println("Allocate " + size + "Blips successfully.");
        // Count the free amount, free fragment amount, max free fragment size and the min one.
        int[] statistics = statistics();
        // Write these to csv.
        writeCSV(statistics);
        // Change the address of all running process.
        collectAddress(location);
        showZones();
        return (location);
    }


    public void collect(int address){
        if (address >= zones.size()){
            System.out.println("No such zone!");
            return;
        }
        Zone tmp = zones.get(address);
        int size = tmp.size;
        if (tmp.isFree) {
            System.out.println("Such zone has been free!");
            return;
        }
        // If the reclaimed partition is not the tail partition and the latter partition is idle,
        // it is merged with the latter partition.
        if (address < zones.size() - 1 && zones.get(address + 1).isFree){
            Zone next = zones.get(address + 1);
            tmp.size += next.size;
            zones.remove(next);
            // Change the address of all handling process.
            allocateAddress(address);
        }
        // If the reclaimed partition is not the first partition and the previous partition is idle,
        // it is merged with the previous partition.
        if (address > 0 && zones.get(address - 1).isFree){
            Zone previous = zones.get(address - 1);
            previous.size += tmp.size;
            zones.remove(address);
            // Change the address of all handling process.
            allocateAddress(address);
            address--;
        }
        zones.get(address).isFree = true;
        System.out.println("Collect successfully! Collect " + size + "Blips!");
        // Count the free amount, free fragment amount, max free fragment size and the min one.
        int[] statistics = statistics();
        // Write to csv,txt is hard to make data aligned.
        writeCSV(statistics);
        showZones();
    }

    /**
     * the number of zones change,so some processes's address change
     * @param address
     */
    public void collectAddress(int address){
        int length = FCFS.runningProcess.size();
        for (int i = 0; i < length; i++){
            int preAddress = FCFS.runningProcess.get(i).getAddress();
            if (preAddress > address){
                FCFS.runningProcess.get(i).setAddress(preAddress + 1);
            }
        }
    }

    /**
     * Change the address.
     * @param address
     */
    public void allocateAddress(int address){
        int length = FCFS.runningProcess.size();
        for (int i = 0; i < length; i++){
            int preAddress = FCFS.runningProcess.get(i).getAddress();
            if (preAddress > address){
                FCFS.runningProcess.get(i).setAddress(preAddress - 1);
            }
        }
    }

    /**
     * Show the zones.
     */
    public void showZones(){
        System.out.println("------------------------------------------------------------");
        System.out.println("Zone ID\tZone head\tZone size\tisFree\t");
        System.out.println("------------------------------------------------------------");
        for (int i = 0; i < zones.size(); i++){
            Zone tmp = zones.get(i);
            System.out.println(i + "\t\t\t\t" + tmp.head + "\t\t\t\t" +
                    tmp.size + "  \t\t\t\t" + tmp.isFree);
        }
        System.out.println("------------------------------------------------------------");
    }

    /**
     * Statistic the free amount, free fragment amount, max free fragment size and the min one.
     * @return
     */
    public int[] statistics(){
        int amount = 0;
        int sum = 0;
        int maxSize = 0;
        int minSize = 0;
        int amountOfMin = 0;
        ArrayList<Integer> size = new ArrayList<>();
        for (Zone eachZone : zones){
            if (eachZone.isFree == true){
                amount++;
                sum += eachZone.size;
                if (eachZone.size <= 5){
                    amountOfMin++;
                }
                size.add(eachZone.size);
            }
        }
        Collections.sort(size);
        maxSize = size.get(amount - 1);
        minSize = size.get(0);
        int[] statistic = {sum, amount, minSize, maxSize, amountOfMin};
        return statistic;
    }

    /**
     * Get all the memory processes from the file.
     * @return
     */
    public static Queue getMemoryProcess(){
        try {         
            BufferedReader readProcess = new BufferedReader(new FileReader("memrun.txt"));
            Queue<MemoryProcess> newProcess = new LinkedList<>();
            String result;
            while ((result = readProcess.readLine()) != null){
                String[] processList;
                processList = result.split("\t");
                // Create a process.
                MemoryProcess newOne = new MemoryProcess(Integer.parseInt(processList[0]), Integer.parseInt(processList[1]), Integer.parseInt(processList[2]));
                newProcess.offer(newOne);
            }
            return newProcess;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Write to the CSV.
     * @param arr
     */
    public void writeCSV(int[] arr){
        try {
            File csv = new File("data.csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(csv, true));
            bw.write("\"" + arr[0] + "\"" + "," + "\"" + arr[1] + "\"" + "," + "\"" + arr[2] + "\"" + "," + "\"" + arr[3] + "\"" + "," + "\"" + arr[4] + "\"");
            bw.newLine();
            bw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

