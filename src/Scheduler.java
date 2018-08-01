import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Scheduler {

	public static int counter = 0;
	// create way to compare two processes
	public static Comparator<Object> comp = new Comparator<Object>() {
        public int compare(Object o1, Object o2) {
        		Process p1 = (Process) o1;
        		Process p2 = (Process) o2;
        		int dif = p1.cycleAdded - p2.cycleAdded;
        		if (dif != 0)
        			return dif;

            int x1 = p1.getArrivalTime();
            int x2 = p2.getArrivalTime();
            int sComp = x1 - x2;

            if (sComp != 0) {
               return sComp;
            } else {
               int lol1 = p1.order;
               int lol2 = p2.order;
               return lol1 - lol2;
            }
        }
	};

	// check if the task is finished
	public static boolean isFinished(ArrayList<Process> pList) {
		boolean sentinel = true;
		for (Process p : pList) {
			if (!p.getState().equals("terminated")) {
				sentinel = false;
			}
		}
		return sentinel;
	}

	// get values from the random file
	public static int randOS(int X) throws IOException {
		File file = new File("random-numbers.txt");
		@SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(new FileReader(file));
		ArrayList<Integer> temp = new ArrayList<Integer>();
		String s;
		while ((s = br.readLine()) != null) {
			temp.add(Integer.parseInt(s));
		}
		// read one line at a time since each number is on a unique line
		int var = 1 + (temp.get(counter) % X);
		counter += 1;
		return var;
	}

	// function to get the process with the lowest remaining cpu time
	public static Process lowestRemCPU(ArrayList<Process> p) {
		ArrayList<Process> curLowest = new ArrayList<>();
        int lowestValue = Integer.MAX_VALUE;
        for (Process p2 : p) {
            if (p2.getRemainingCPU() < lowestValue) {
                curLowest = new ArrayList<Process>();
                curLowest.add(p2);
                lowestValue = p2.getRemainingCPU();
            } else if (p2.getRemainingCPU() == lowestValue) {
                curLowest.add(p2);
            }
        }

        Collections.sort(curLowest, comp);
        return curLowest.get(0);

	}

	public static void main(String[] args) throws IOException {
		DecimalFormat df = new DecimalFormat("#.######");
		File file = new File((args.length == 2 ? args[1] : args[0]));
		boolean isVerbose = false;
		// if the input is verbose then the output will be more detailed
		if (args.length == 2 && args[0].equals("--verbose")) {
			isVerbose = true;
		}
		@SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(new FileReader(file));
		// different scheduling algorithms
		ArrayList<Process> pList = new ArrayList<Process>();
		ArrayList<Process> rrList = new ArrayList<Process>();
		ArrayList<Process> uniList = new ArrayList<Process>();
		ArrayList<Process> psjfList = new ArrayList<Process>();

		StringBuilder stored = new StringBuilder();
		String s;

		while ((s = br.readLine()) != null) {
			stored.append(s.trim() + " ");
		}

		// parsing data
		String temp1 = stored.toString();
		String[] arr = temp1.split("\\s+");

		int runTime = Integer.parseInt(arr[0]);
		int start = 1;
		// traverse input file to get necessary information
		for (int i = 0; i < runTime; i++) {
			Process temp = new Process(Integer.parseInt(arr[start]), Integer.parseInt(arr[start + 1]), Integer.parseInt(arr[start + 2]), Integer.parseInt(arr[start + 3]));
			pList.add(temp);
			temp.order = i;
			start += 4;
		}
		System.out.print("The original input was: " + runTime + "  ");
		for (Process p : pList) {
			System.out.print(Integer.toString(p.getArrivalTime()) + " " + Integer.toString(p.getBound()) + " " + Integer.toString(p.getCPUTime()) + " " + Integer.toString(p.getIOTime()) + "  ");
		}
		System.out.println();

		Collections.sort(pList, comp);

		System.out.print("The (sorted) input is: " + runTime + "  ");
		for (Process p : pList) {
			System.out.print(Integer.toString(p.getArrivalTime()) + " " + Integer.toString(p.getBound()) + " " + Integer.toString(p.getCPUTime()) + " " + Integer.toString(p.getIOTime()) + "  ");
		}

		for (Process p : pList) {
			rrList.add((Process) p.clone());
			uniList.add((Process) p.clone());
			psjfList.add((Process) p.clone());
		}

		System.out.println("\n");
		if (!isVerbose)
			System.out.println("The scheduling algorithm used was First Come First Served");
		else
			System.out.println("This detailed printout gives the state and remaining burst for each process");

		int timer = 0;
		ArrayList<Process> readyQ = new ArrayList<Process>();
		int totalWait = 0;
		counter = 0;

		// FCFS
		while (!isFinished(pList)) {
			if (isVerbose)
				System.out.print("Before cycle " + timer + ": ");
			Process running = null;
			for (Process p : pList) {
				if (isVerbose)
					System.out.print(p);
				if (p.getState().equals("blocked")) {
					p.deincrementIO();
					if (p.getIoBurst() <= 0) {
						p.setState("ready");
						readyQ.add(p);
					}
				}
			}
			if (isVerbose)
				System.out.println();

			for (Process p : pList) {
				if (p.getState().equals("unstarted") && timer >= p.getArrivalTime()) {
					p.setState("ready");
					readyQ.add(p);
				}
				if (p.getState().equals("running")) {
					running = p;
				}
			}

			if (running != null) {
				running.setRemainingCPU(running.getRemainingCPU() - 1);
				running.setCpuBurst(running.getCpuBurst() - 1);
				if (running.getRemainingCPU() <= 0) {
					running.setState("terminated");
					running.setFinishTime(timer);
					running = null;
				} else if (running.getCpuBurst() <= 0) {
					running.setState("blocked");
					running.setIoBurst(randOS(running.getIOTime()));
					running = null;
				}
			}
			if (!readyQ.isEmpty() && running == null) {
				Process p = readyQ.remove(0);
				p.setState("running");
				p.setCpuBurst(randOS(p.getBound()));
			}
			boolean alreadyCounted = false;
			for (Process p : pList) {
				if (p.getState().equals("running")) {
					p.setRunTime(p.getRunTime() + 1);
					p.setFinishTime(p.getFinishTime() + 1);
				}
				if (p.getState().equals("blocked")) {
					p.setFinishTime(p.getFinishTime() + 1);
					p.setTimeInIO(p.getTimeInIO() + 1);
					if (!alreadyCounted)
						totalWait += 1;
					alreadyCounted = true;
				}
				if (p.getState().equals("ready")) {
					p.setWaitingTime(p.getWaitingTime() + 1);
				}
			}
			timer += 1;
		}
		if (isVerbose)
			System.out.println("The scheduling algorithm used was First Come First Served");
		System.out.println();
		int totalRun = 0;
		int totalTurn = 0;
		int collectiveWait = 0;
		for (Process p : pList) {
			totalRun += p.getRunTime();
			collectiveWait += p.getWaitingTime();
			totalTurn += p.getTurnTime();
		}

		for (int i = 0; i < runTime; i++) {
			System.out.println("Process " + i + ":");
			System.out.println("\t(A, B, C, IO) = (" + pList.get(i).getArrivalTime() + ", " + pList.get(i).getBound() + ", " + pList.get(i).getCPUTime() + ", " + pList.get(i).getIOTime() + ")");
			System.out.println("\tFinishing time: " + pList.get(i).getFinishTime());
			System.out.println("\tTurnaround time: " + pList.get(i).getTurnTime());
			System.out.println("\tI/O time: " + pList.get(i).getTimeInIO());
			System.out.println("\tWaiting time: " + pList.get(i).getWaitingTime());
			System.out.println();
		}

		System.out.println("Summary Data: ");
		System.out.println("\tFinishing time: " + (timer - 1));
		System.out.println("\tCPU Utilization: " + df.format((float) totalRun / (timer - 1)));
		System.out.println("\tI/O Utilization: " + df.format((float) totalWait / (timer - 1)));
		System.out.println("\tThroughput: " + df.format((float) runTime / ((timer - 1) / 100.0)) + " processes per hundred cycles");
		System.out.println("\tAverage turnaround time: " + df.format((float) totalTurn / runTime));
		System.out.println("\tAverage waiting time: " + df.format((float) collectiveWait / runTime));

		///////////////

		System.out.println("\n");
		if (!isVerbose)
			System.out.println("The scheduling algorithm used was Round Robin");
		else
			System.out.println("This detailed printout gives the state and remaining burst for each process");
		readyQ.clear();
		timer = 0;
		totalWait = 0;
		counter = 0;

		// RR
		while (!isFinished(rrList)) {
			if (isVerbose)
				System.out.print("Before cycle " + timer + ": ");
			Process running = null;
			for (Process p : rrList) {
				if (isVerbose)
					System.out.print(p.rrStr());
				if (p.getState().equals("blocked")) {
					p.deincrementIO();
					if (p.getIoBurst() <= 0) {
						p.setState("ready");
						readyQ.add(p);
						p.cycleAdded = timer;
					}
				}
			}
			if (isVerbose)
				System.out.println();

			for (Process p : rrList) {
				if (p.getState().equals("unstarted") && timer >= p.getArrivalTime()) {
					p.setState("ready");
					readyQ.add(p);
					p.cycleAdded = timer;
				}
				if (p.getState().equals("running")) {
					running = p;
				}
			}

			if (running != null) {
				running.setRemainingCPU(running.getRemainingCPU() - 1);
				running.setCpuBurst(running.getCpuBurst() - 1);
				running.setRemainingQuantum(running.getRemainingQuantum() - 1);
				if (running.getRemainingCPU() <= 0) {
					running.setState("terminated");
					running.setFinishTime(timer);
					running = null;
				} else if (running.getCpuBurst() <= 0) {
					running.setState("blocked");
					running.setIoBurst(randOS(running.getIOTime()));
					running = null;
				} else if (running.getRemainingQuantum() <= 0) {
					running.setRemainingQuantum(Math.min(2, running.getCpuBurst()));
					running.setState("ready");
					readyQ.add(running);
					running.cycleAdded = timer;
					running = null;
				}
			}

			if (!readyQ.isEmpty() && running == null) {
				Collections.sort(readyQ, comp);
				Process p = readyQ.remove(0);
				p.setState("running");
				if (p.getCpuBurst() <= 0) {
					int a = randOS(p.getBound());
					if (a >= 2) {
						p.setCpuBurst(a);
						p.setRemainingQuantum(2);
					} else {
						p.setCpuBurst(1);
						p.setRemainingQuantum(1);
					}
				}
			}

			boolean alreadyCounted = false;
			for (Process p : rrList) {
				if (p.getState().equals("running")) {
					p.setRunTime(p.getRunTime() + 1);
					p.setFinishTime(p.getFinishTime() + 1);
				}
				if (p.getState().equals("blocked")) {
					p.setFinishTime(p.getFinishTime() + 1);
					p.setTimeInIO(p.getTimeInIO() + 1);
					if (!alreadyCounted)
						totalWait += 1;
					alreadyCounted = true;
				}
				if (p.getState().equals("ready")) {
					p.setWaitingTime(p.getWaitingTime() + 1);
				}
			}
			timer += 1;
		}

		if (isVerbose)
			System.out.println("The scheduling algorithm used was Round Robin");
		System.out.println();
		totalRun = 0;
		totalTurn = 0;
		collectiveWait = 0;
		
		for (Process p : rrList) {
			totalRun += p.getRunTime();
			collectiveWait += p.getWaitingTime();
			totalTurn += p.getTurnTime();
		}

		for (int i = 0; i < runTime; i++) {
			System.out.println("Process " + i + ":");
			System.out.println("\t(A, B, C, IO) = (" + rrList.get(i).getArrivalTime() + ", " + rrList.get(i).getBound() + ", " + rrList.get(i).getCPUTime() + ", " + rrList.get(i).getIOTime() + ")");
			System.out.println("\tFinishing time: " + rrList.get(i).getFinishTime());
			System.out.println("\tTurnaround time: " + rrList.get(i).getTurnTime());
			System.out.println("\tI/O time: " + rrList.get(i).getTimeInIO());
			System.out.println("\tWaiting time: " + rrList.get(i).getWaitingTime());
			System.out.println();
		}

		System.out.println("Summary Data: ");
		System.out.println("\tFinishing time: " + (timer - 1));
		System.out.println("\tCPU Utilization: " + df.format((float) totalRun / (timer - 1)));
		System.out.println("\tI/O Utilization: " + df.format((float) totalWait / (timer - 1)));
		System.out.println("\tThroughput: " + df.format((float) runTime / ((timer - 1) / 100.0)) + " processes per hundred cycles");
		System.out.println("\tAverage turnaround time: " + df.format((float) totalTurn / runTime));
		System.out.println("\tAverage waiting time: " + df.format((float) collectiveWait / runTime));

		////////

		System.out.println("\n");
		if (!isVerbose)
			System.out.println("The scheduling algorithm used was Uniprocessor");
		else
			System.out.println("This detailed printout gives the state and remaining burst for each process");
		readyQ.clear();
		timer = 0;
		totalWait = 0;
		counter = 0;

		// uni
		while (!isFinished(uniList)) {
			if (isVerbose)
				System.out.print("Before cycle " + timer + ": ");
			Process running = null;
			boolean current = false;
			for (Process p : uniList) {
				if (isVerbose)
					System.out.print(p);
				if (p.getState().equals("blocked")) {
					p.deincrementIO();
					if (p.getIoBurst() <= 0) {
						p.setState("ready");
						readyQ.add(0, p);
					} else {
						current = true;
					}
				}
			}
			if (isVerbose)
				System.out.println();

			for (Process p : uniList) {
				if (p.getState().equals("unstarted") && timer >= p.getArrivalTime()) {
					p.setState("ready");
					readyQ.add(p);
				}
				if (p.getState().equals("running")) {
					running = p;
				}
			}

			if (running != null) {
				running.setRemainingCPU(running.getRemainingCPU() - 1);
				running.setCpuBurst(running.getCpuBurst() - 1);
				if (running.getRemainingCPU() <= 0) {
					running.setState("terminated");
					running.setFinishTime(timer);
					running = null;
				} else if (running.getCpuBurst() <= 0) {
					running.setState("blocked");
					running.setIoBurst(randOS(running.getIOTime()));
				}
			}
			if (!readyQ.isEmpty() && running == null && current == false) {
				Process p = readyQ.remove(0);
				p.setState("running");
				p.setCpuBurst(randOS(p.getBound()));
			}
			boolean alreadyCounted = false;
			for (Process p : uniList) {
				if (p.getState().equals("running")) {
					p.setRunTime(p.getRunTime() + 1);
					p.setFinishTime(p.getFinishTime() + 1);
				}
				if (p.getState().equals("blocked")) {
					p.setFinishTime(p.getFinishTime() + 1);
					p.setTimeInIO(p.getTimeInIO() + 1);
					if (!alreadyCounted)
						totalWait += 1;
					alreadyCounted = true;
				}
				if (p.getState().equals("ready")) {
					p.setWaitingTime(p.getWaitingTime() + 1);
				}
			}
			timer += 1;
		}
		if (isVerbose)
			System.out.println("The scheduling algorithm used was Uniprocessor");
		System.out.println();
		totalRun = 0;
		totalTurn = 0;
		collectiveWait = 0;
		for (Process p : uniList) {
			totalRun += p.getRunTime();
			collectiveWait += p.getWaitingTime();
			totalTurn += p.getTurnTime();
		}

		for (int i = 0; i < runTime; i++) {
			System.out.println("Process " + i + ":");
			System.out.println("\t(A, B, C, IO) = (" + uniList.get(i).getArrivalTime() + ", " + uniList.get(i).getBound() + ", " + uniList.get(i).getCPUTime() + ", " + uniList.get(i).getIOTime() + ")");
			System.out.println("\tFinishing time: " + uniList.get(i).getFinishTime());
			System.out.println("\tTurnaround time: " + uniList.get(i).getTurnTime());
			System.out.println("\tI/O time: " + uniList.get(i).getTimeInIO());
			System.out.println("\tWaiting time: " + uniList.get(i).getWaitingTime());
			System.out.println();
		}

		System.out.println("Summary Data: ");
		System.out.println("\tFinishing time: " + (timer - 1));
		System.out.println("\tCPU Utilization: " + df.format((float) totalRun / (timer - 1)));
		System.out.println("\tI/O Utilization: " + df.format((float) totalWait / (timer - 1)));
		System.out.println("\tThroughput: " + df.format((float) runTime / ((timer - 1) / 100.0)) + " processes per hundred cycles");
		System.out.println("\tAverage turnaround time: " + df.format((float) totalTurn / runTime));
		System.out.println("\tAverage waiting time: " + df.format((float) collectiveWait / runTime));

		//////////////////

		System.out.println("\n");
		if (!isVerbose)
			System.out.println("The scheduling algorithm used was Preemptive Shortest Job First");
		else
			System.out.println("This detailed printout gives the state and remaining burst for each process");

		readyQ.clear();
		timer = 0;
		totalWait = 0;
		counter = 0;

		// psjf
		while (!isFinished(psjfList)) {
			if (isVerbose)
				System.out.print("Before cycle " + timer + ": ");
			Process running = null;
			for (Process p : psjfList) {
				if (isVerbose)
					System.out.print(p);
				if (p.getState().equals("blocked")) {
					p.deincrementIO();
					if (p.getIoBurst() <= 0) {
						p.setState("ready");
						readyQ.add(p);
					}
				}
			}
			if (isVerbose)
				System.out.println();

			for (Process p : psjfList) {
				if (p.getState().equals("unstarted") && timer >= p.getArrivalTime()) {
					p.setState("ready");
					readyQ.add(p);
				}
				if (p.getState().equals("running")) {
					running = p;
				}
			}

			if (running != null) {
				running.setRemainingCPU(running.getRemainingCPU() - 1);
				running.setCpuBurst(running.getCpuBurst() - 1);
				if (running.getRemainingCPU() <= 0) {
					running.setState("terminated");
					running.setFinishTime(timer);
					running = null;
				} else if (running.getCpuBurst() <= 0) {
					running.setState("blocked");
					running.setIoBurst(randOS(running.getIOTime()));
					running = null;
				}
			}

			if (!readyQ.isEmpty()) {
				if (running == null) {
					Process temp = lowestRemCPU(readyQ);
					Process p = readyQ.remove(readyQ.indexOf(temp));
					p.setState("running");
					if (p.getCpuBurst() <= 0)
						p.setCpuBurst(randOS(p.getBound()));
				} else {
					Process temp = lowestRemCPU(readyQ);
					if (temp.getRemainingCPU() <= running.getRemainingCPU()) {
						readyQ.remove(readyQ.indexOf(temp));
						temp.setState("running");
						if (temp.getCpuBurst() <= 0)
							temp.setCpuBurst(randOS(temp.getBound()));
						running.setState("ready");
						readyQ.add(running);
						running = null;
					}
				}
			}

			boolean alreadyCounted = false;
			for (Process p : psjfList) {
				if (p.getState().equals("running")) {
					p.setRunTime(p.getRunTime() + 1);
					p.setFinishTime(p.getFinishTime() + 1);
				}
				if (p.getState().equals("blocked")) {
					p.setFinishTime(p.getFinishTime() + 1);
					p.setTimeInIO(p.getTimeInIO() + 1);
					if (!alreadyCounted)
						totalWait += 1;
					alreadyCounted = true;
				}
				if (p.getState().equals("ready")) {
					p.setWaitingTime(p.getWaitingTime() + 1);
				}
			}
			timer += 1;
		}
		if (isVerbose)
			System.out.println("The scheduling algorithm used was Preemptive Shortest Job First");
		System.out.println();
		totalRun = 0;
		totalTurn = 0;
		collectiveWait = 0;
		for (Process p : psjfList) {
			totalRun += p.getRunTime();
			collectiveWait += p.getWaitingTime();
			totalTurn += p.getTurnTime();
		}

		for (int i = 0; i < runTime; i++) {
			System.out.println("Process " + i + ":");
			System.out.println("\t(A, B, C, IO) = (" + psjfList.get(i).getArrivalTime() + ", " + psjfList.get(i).getBound() + ", " + psjfList.get(i).getCPUTime() + ", " + psjfList.get(i).getIOTime() + ")");
			System.out.println("\tFinishing time: " + psjfList.get(i).getFinishTime());
			System.out.println("\tTurnaround time: " + psjfList.get(i).getTurnTime());
			System.out.println("\tI/O time: " + psjfList.get(i).getTimeInIO());
			System.out.println("\tWaiting time: " + psjfList.get(i).getWaitingTime());
			System.out.println();
		}

		System.out.println("Summary Data: ");
		System.out.println("\tFinishing time: " + (timer - 1));
		System.out.println("\tCPU Utilization: " + df.format((float) totalRun / (timer - 1)));
		System.out.println("\tI/O Utilization: " + df.format((float) totalWait / (timer - 1)));
		System.out.println("\tThroughput: " + df.format((float) runTime / ((timer - 1) / 100.0)) + " processes per hundred cycles");
		System.out.println("\tAverage turnaround time: " + df.format((float) totalTurn / runTime));
		System.out.println("\tAverage waiting time: " + df.format((float) collectiveWait / runTime));
		System.out.println();

	}
}
