// Larry Liu

public class Process implements Comparable<Process>, Cloneable {
	// variables to keep track of
	private int a;
	private int b;
	private int c;
	private int m;
	private String state;
	private int finishTime;
	private int timeInIO;
	private int waitingTime;
	private int quantum;
	private int remainingCPU;
	private int ioBurst;
	private int runTime;
	private int cpuBurst;
	private int remainingQuantum;
	public int order;
	public int cycleAdded;

	// getters and setters
	public int getRemainingQuantum() {
		return remainingQuantum;
	}

	public void setRemainingQuantum(int remainingQuantum) {
		this.remainingQuantum = remainingQuantum;
	}

	// create copy without same reference to allow for modification
	public Object clone() {
		return new Process(this);
	}

	// constructor
	public Process (Process p) {
		a = p.a;
		b = p.b;
		c = p.c;
		m = p.m;
		state = p.state;
		finishTime = p.finishTime;
		timeInIO = p.timeInIO;
		runTime = p.runTime;
		waitingTime = p.waitingTime;
		quantum = p.quantum;
		remainingCPU = p.remainingCPU;
		cpuBurst = p.cpuBurst;
		remainingQuantum = p.remainingQuantum;
		ioBurst = p.ioBurst;
		cycleAdded = p.cycleAdded;
		order = p.order;
	}

	// constructor with different initialization
	public Process (int arrival, int bound, int cpu, int io) {
	    a = arrival;
	    b = bound;
	    c = cpu;
	    m = io;
	    state = "unstarted";
	    finishTime = 0;
	    timeInIO = 0;
	    runTime = 0;
	    waitingTime = 0;
			// default
	    quantum = 2;
	    remainingCPU = cpu;
	    cpuBurst = 0;
	    remainingQuantum = quantum;
	    cycleAdded = 0;
	}

	// getters and setters
	public int getCpuBurst() {
		return cpuBurst;
	}

	public void setCpuBurst(int cpuBurst) {
		this.cpuBurst = cpuBurst;
	}

	public int getRunTime() {
		return runTime;
	}

	public void setRunTime(int runTime) {
		this.runTime = runTime;
	}

	public int getTimeInIO() {
		return timeInIO;
	}

	public void setTimeInIO(int timeInIO) {
		this.timeInIO = timeInIO;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public int getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(int finishTime) {
		this.finishTime = finishTime;
	}

	public int getTurnTime() {
		return this.finishTime - this.getArrivalTime();
	}

	public int getWaitingTime() {
		return waitingTime;
	}

	public void setWaitingTime(int waitingTime) {
		this.waitingTime = waitingTime;
	}

	public int getQuantum() {
		return quantum;
	}

	public void setQuantum(int quantum) {
		this.quantum = quantum;
	}

	public int getRemainingCPU() {
		return remainingCPU;
	}

	// check state for termination
	public void setRemainingCPU(int remainingCPU) {
		this.remainingCPU = remainingCPU;
		if (remainingCPU <= 0) {
			this.state = "terminated";
		}
	}

	public int getArrivalTime() {
	    return a;
	}

	public int getIoBurst() {
		return ioBurst;
	}

	public void setIoBurst(int ioBurst) {
		this.ioBurst = ioBurst;
	}

	public void setArrivalTime(int arrivalTime) {
	  this.a = arrivalTime;
	}

	public int getBound() {
	  return b;
	}

	public void setBound(int bound) {
	  this.b = bound;
	}

	public int getCPUTime() {
	  return c;
	}

	public void setCPUTime(int cpuTime) {
	  this.c = cpuTime;
	}

	public int getIOTime() {
	  return m;
	}

	public void setIOTime(int ioTime) {
	  this.m = ioTime;
	}

	// deincrementation functions
	public void deincrementCPU() {
		this.cpuBurst -= 1;
	}

	public void deincrementIO() {
		this.ioBurst -= 1;
	}

	@Override
	public int compareTo(Process o) {
		// TODO Auto-generated method stub
		return lab2.comp.compare(this, o);
	}

	// printing function
	public String toString() {
		if (this.state.equals("unstarted") || this.state.equals("terminated"))
			return this.state + " 0 ";
		else if (this.state.equals("running") || this.state.equals("ready"))
			return this.state + " " + this.cpuBurst + " ";
		else
			return this.state + " " + this.ioBurst + " ";
//		return this.state + " " + this.cycleAdded + " " + this.order;
	}

	public String getMemory() {
		return super.toString();
	}

	// special print for round robin
	public String rrStr() {
		if (this.state.equals("unstarted") || this.state.equals("terminated"))
			return this.state + " 0 ";
		else if (this.state.equals("running") || this.state.equals("ready"))
			return this.state + " " + this.remainingQuantum + " ";
		else
			return this.state + " " + this.ioBurst + " ";
	}
}
