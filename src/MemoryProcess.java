public class MemoryProcess {
    private int timeStamp;
    private int size;
    private int timeRequired;
    private int address;

    public MemoryProcess(int timeStamp, int size, int timeRequired) {
        this.timeStamp = timeStamp;
        this.size = size;
        this.timeRequired = timeRequired;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTimeRequired() {
        return timeRequired;
    }

    public void setTimeRequired(int timeRequired) {
        this.timeRequired = timeRequired;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

}
