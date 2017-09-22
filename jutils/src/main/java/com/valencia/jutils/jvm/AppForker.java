package com.valencia.jutils.jvm;
/**
 * 
 */


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * <p>Runs specified programs in separate JVMs. Supports setting the heap size for each of the JVMs separately 
 * as well as a timeout after which the JVMs will be killed.
 * 
 * <p>In order to see output from this class as well as the output of the separate JVMs, enable INFO or higher level 
 * logging for this class in the log4j settings. The output of each of the separate JVMs will be printed to the console 
 * as well as to a log file prefixed with "MultiJVMRunner_" placed in the current working directory. Note that this file 
 * will be overwritten every time this runner executes.
 */
public class AppForker {

	protected static final Logger logger = Logger.getLogger(AppForker.class);

	private List<String[]> programArgs = new ArrayList<>();
	private long waitTimeBeforeKill = Long.MAX_VALUE;
	private HeapSpec[] heapSpecs;
	private boolean waitForJVMs = true;
	private List<String> junitClasses = new ArrayList<>();
	private String[] setupProgramArgs;
	private HeapSpec setupProgramHeapSpec;

    private File redirectOutputFile;

    private boolean redirectOutputToConsoleAndLogFile = true;

	public AppForker(List<String[]> programArgs) {
		this.programArgs.clear();
		this.programArgs.addAll(programArgs);
	}

	/**
	 * 
	 * @param programArgs The program arguments for each of the JVMs.
	 */
	public AppForker(String[]... programArgs) {
		this.setCommandArgs(programArgs);
	}

	public AppForker(long waitTimeBeforeKill, String[]... programArgs) {
		this(programArgs);
		this.waitTimeBeforeKill = waitTimeBeforeKill;
	}

	public AppForker(long waitTimeBeforeKill, HeapSpec[] heapSpecs, String... programArgs) {
		this(waitTimeBeforeKill, programArgs);
		this.heapSpecs = heapSpecs;
	}

	/**
	 * Returns the program arguments for each of the JVMs. The order of the <code>List</code> is the order of the JVMs.
	 */
	public List<String[]> getProgramArgs() {
		return this.programArgs;
	}

	public void setCommandArgs(String[]... commandArgs) {
		this.programArgs.clear();
		for (String[] args : commandArgs) {
			this.programArgs.add(args);
		}
	}

	/**
	 * Returns the time to wait in ms before killing the additional JVMs. Ignored if {@link #isWaitForJVMs()} returns <code>false</code>. 
	 * Default wait time is {@link Long#MAX_VALUE}.
	 */
	public long getWaitTimeBeforeKill() {
		return this.waitTimeBeforeKill;
	}

	public void setWaitTimeBeforeKill(long waitTimeBeforeKill) {
		this.waitTimeBeforeKill = waitTimeBeforeKill;
	}

	/**
	 * Returns the heap size specifications for the JVMs. The order of the array is the order of the JVMs.
	 */
	public HeapSpec[] getHeapSpecs() {
		return this.heapSpecs;
	}

	public void setHeapSpecs(HeapSpec[] heapSpecs) {
		this.heapSpecs = heapSpecs;
	}

	/**
	 * Returns whether the {@link #execute()} method will wait for the JVMs to terminate. Default is <code>true</code>.
	 */
	public boolean isWaitForJVMs() {
		return this.waitForJVMs;
	}

	public void setWaitForJVMs(boolean waitForJVMs) {
		this.waitForJVMs = waitForJVMs;
	}

	public List<String> getJunitClasses() {
		return this.junitClasses;
	}

	public void setJunitClasses(List<String> junitClasses) {
		this.junitClasses = junitClasses;
	}

	public String[] getSetupProgramArgs() {
		return this.setupProgramArgs;
	}

	public void setSetupProgramArgs(String[] setupProgramArgs) {
		this.setupProgramArgs = setupProgramArgs;
	}

	public HeapSpec getSetupProgramHeapSpec() {
		return this.setupProgramHeapSpec;
	}

	public void setSetupProgramHeapSpec(HeapSpec setupProgramHeapSpec) {
		this.setupProgramHeapSpec = setupProgramHeapSpec;
	}
	
	public File getRedirectOutputFile() {
        return redirectOutputFile;
    }

    public void setRedirectOutputFile(File redirectOutputFile) {
        this.redirectOutputFile = redirectOutputFile;
    }

    public boolean isRedirectOutputToConsoleAndFile() {
        return redirectOutputToConsoleAndLogFile;
    }

    public void setRedirectOutputToConsoleAndFile(boolean read) {
        this.redirectOutputToConsoleAndLogFile = read;
    }

    public static final String JUNIT_RUNNER_CLASS_NAME = "org.junit.runner.JUnitCore";

	/**
	 * <p>Starts the programs specified in {@link #getProgramArgs()} each in its own JVM. If {@link #isWaitForJVMs()} returns 
	 * <code>true</code>, this method will block until all the processes terminate or the time specified by 
	 * {@link #getWaitTimeBeforeKill()} elapses, after which the processes will be terminated. Otherwise the method 
	 * will return immediately and leave the newly created child processes running.
	 * 
	 * <p>After this method returns, a call to {@link #close()} should be made.
	 * 
	 * @return A <code>Map</code> of the <code>ProcessBuilder</code> and associated <code>Process</code> instances that 
	 * were created and executed.
	 * 
	 * @throws Exception
	 */
	public Map<ProcessBuilder, Process> execute() throws Exception {
		List<ProcessBuilder> procBuilders = new ArrayList<>();
		int procIndex = 0;
		for (String[] args : this.programArgs) {
			List<String> progArgs = this.getCoreProgArgs(procIndex);
			for (String arg : args) {
				progArgs.add(arg);
			}

			ProcessBuilder processBuilder = new ProcessBuilder(progArgs);
			if (this.redirectOutputFile != null) {
			    processBuilder.redirectOutput(this.redirectOutputFile);
			}
			// redirect stderr to stdin so its merged
			processBuilder.redirectErrorStream(true);
			procBuilders.add(processBuilder);
			procIndex++;
		}

		if (this.junitClasses.size() > 0) {
			procIndex = 0;
			for (String className : this.junitClasses) {
				List<String> progArgs = this.getCoreProgArgs(procIndex);
				progArgs.add(JUNIT_RUNNER_CLASS_NAME);
				progArgs.add(className);

				ProcessBuilder processBuilder = new ProcessBuilder(progArgs);
				// redirect stderr to stdin so its merged
				processBuilder.redirectErrorStream(true);
				procBuilders.add(processBuilder);
				procIndex++;
			}
		}

		if (this.setupProgramArgs != null && this.setupProgramArgs.length > 0) {
			procIndex = -1;
			List<String> progArgs = this.getCoreProgArgs(procIndex, this.setupProgramHeapSpec);
			for (String arg : this.setupProgramArgs) {
				progArgs.add(arg);
			}

			ProcessBuilder processBuilder = new ProcessBuilder(progArgs);
			if (logger.isEnabledFor(Level.INFO)) {
				logger.info("Starting setup program with args: " + StringUtils.join(processBuilder.command().iterator(), " "));
			}
			// redirect stderr to stdin so its merged
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();
			if (this.redirectOutputFile == null && this.redirectOutputToConsoleAndLogFile) {
	            // separate thread will print stdout and stderr to console and log file
	            StreamReaderThread ioThread = getStreamReaderThread(procIndex, process);
	            ioThread.start();
	            this.ioThreads.add(ioThread);
			}
			this.waitForJVM(processBuilder, process);
		}

		procIndex = 0;
		Map<ProcessBuilder, Process> jvms = new HashMap<>();
		for (ProcessBuilder builder : procBuilders) {
			if (logger.isEnabledFor(Level.INFO)) {
				logger.info("Starting JVM with args: " + StringUtils.join(builder.command().iterator(), " "));
			}
			Process process = builder.start();
            if (this.redirectOutputFile == null && this.redirectOutputToConsoleAndLogFile) {
                // separate thread will print stdout and stderr to console and log file
                StreamReaderThread ioThread = getStreamReaderThread(procIndex, process);
                ioThread.start();
                this.ioThreads.add(ioThread);
            }
			jvms.put(builder, process);
			procIndex++;
		}

		if (this.isWaitForJVMs()) {
			for (Entry<ProcessBuilder, Process> entry : jvms.entrySet()) {
				ProcessBuilder builder = entry.getKey();
				Process jvm = entry.getValue();
				this.waitForJVM(builder, jvm);
			}
		}

		return jvms;
	}
	
	   /**
     * Sends the specified string to the standard input stream of the specified process. Note that a sleep might be needed after sending a 
     * command and before sending another one to ensure that the process can receive and process the command. This will vary based on how much 
     * time the program needs to process the command and wait for new input. 
     * 
     * @param proc The process to which to send the string.
     * @param cmd The string to send to the process.
     * @param sleepAfterMs The amount of time to sleep after executing the command.
     * 
     * @throws IOException
     */
    public static void sendCmdToProcess(Process proc, String cmd, long sleepAfterMs) throws IOException {
        OutputStream outputStream = proc.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(outputStream);
        BufferedWriter writer = new BufferedWriter(osw);
        try {
            writer.write(cmd);
            writer.newLine();
            writer.flush();
        } finally {
            try {
                Thread.sleep(sleepAfterMs);
            } catch (InterruptedException e) {
            }
        }
    }
    
    /**
     * Reads the standard output stream of the specified process.
     * 
     * @param proc
     *            The process to which to send the string.
     * @param cmd
     *            The string to send to the process.
     * @param sleepAfterMs
     *            The amount of time to sleep after executing the command.
     * 
     * @throws IOException
     */
    public static String readProcessOutput(Process proc) throws IOException {
        InputStream inputStream = proc.getInputStream();
        try {
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder b = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                b.append(line);
            }
            return b.toString();
        } finally {
            inputStream.close();
        }
    }

	/**
	 * Closes any resources allocated after a call to {@link #execute()}.
	 */
	public void close() {
		for (StreamReaderThread thread : this.ioThreads) {
			thread.stopAndClose();
		}
	}

	private StreamReaderThread getStreamReaderThread(int procIndex, Process process) {
		StreamReaderThread ioThread = new StreamReaderThread(process.getInputStream(), "JVM#" + (procIndex + 1), false);
		return ioThread;
	}

	private void waitForJVM(ProcessBuilder builder, Process jvm) {
		String args = StringUtils.join(builder.command().iterator(), " ");
		if (logger.isEnabledFor(Level.INFO)) {
			logger.info("Waiting for exit of JVM with args: " + args);
		}

		final Thread runnerThread = Thread.currentThread();
		final long waitTime = this.waitTimeBeforeKill;
		// implement wait in a separate thread and interrupt this one to support timeout
		Thread killThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(waitTime);
					runnerThread.interrupt();
				} catch (InterruptedException e) {
					logger.debug("Kill thread was interrupted");
				}
			}
		}, "MultiJVMRunnerKillThread");
		killThread.start();

		try {
			int exitCode = jvm.waitFor();
			logger.info("JVM exited with code " + exitCode + " for ags " + args);
			killThread.interrupt();
		} catch (InterruptedException e) {
			logger.info("Timeout waiting for JVM to finish, terminating JVM with args " + args);
			jvm.destroy();
		}
	}

	private List<String> getCoreProgArgs(int procIndex) {
		HeapSpec heapSpec = null;
		if (this.heapSpecs != null && this.heapSpecs.length > 0) {
			if (this.heapSpecs.length == 1) {
				// use the same heap spec for all
				heapSpec = this.heapSpecs[0];
			} else {
				heapSpec = this.heapSpecs[procIndex];
			}
		}

		return this.getCoreProgArgs(procIndex, heapSpec);
	}

	private List<String> getCoreProgArgs(int procIndex, HeapSpec heapSpec) {
		String cwd = System.getProperty("user.dir");
		String separator = System.getProperty("file.separator");
		// support execution from RAD/Eclipse
		String classpath = System.getProperty("java.class.path") + File.pathSeparator + cwd
				+ File.pathSeparator + cwd + File.separator + "test";
		if (Platform.get().equals(Platform.WINDOWS)) {
			// surround with quotes in windows platform
			classpath = "\"" + classpath + "\"";
		}
		String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
		if (Platform.get().equals(Platform.WINDOWS)) {
			// surround with quotes in windows platform
			path = "\"" + path + "\"";
		}

		List<String> progArgs = new ArrayList<>();
		progArgs.add(path);
		if (heapSpec != null) {
			String heapSpecStr = heapSpec.getJavaCmdString();
			progArgs.add(heapSpecStr);
		}
		progArgs.add("-cp");
		progArgs.add(classpath);
		return progArgs;
	}

	/*
	 * a thread to keep flushing out the process outputs
	 */
	private class StreamReaderThread extends Thread {
		InputStream is;
		String name;
		StringBuilder buffer;
		FileWriter fileOs;

		@SuppressWarnings("unused")
		boolean consumeCompleted = false; // TODO: Might need a fix - volatile 

		private StreamReaderThread(InputStream is, String name) {
			this(is, name, true);
		}
		private StreamReaderThread(InputStream is, String name, boolean bufferData) {
			this.setName("StreamReaderThread_" + name);
			this.is = is;
			this.name = name;
			if (bufferData) {
				buffer = new StringBuilder(1024);
			}
			File file = new File("AppForker_" + this.name + ".log");
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					logger.warn("An exception occurred creating child process log " + name, e);
				}
			}
			try {
				fileOs = new FileWriter(file);
			} catch (IOException e) {
				logger.warn("An exception occurred creating child process log " + name, e);
			}
		}

		public void run() {
			consumeStream(is, name, fileOs, buffer, true);
			consumeCompleted = true;
		}

		@SuppressWarnings("unused")
		public StringBuilder getOutputBuffer() {
			return buffer;
		}

		void stopAndClose() {
			if (isAlive()) {
				try {
					interrupt();
					join();
				} catch (Exception e) {
					logger.log(Level.DEBUG, "", e);
				}
			}

			if (null != is) {
				try {
					is.close();
				} catch (Exception e) {
					logger.log(Level.DEBUG, "", e);
				}
			}
		}
	}

    public static void consumeStream(InputStream is, String streamName, FileWriter fileOs, StringBuilder outputBuffer, boolean consumeFully) {
        consumeStreamFast(is, streamName, fileOs, outputBuffer, consumeFully);
    }

    public static void consumeStreamFast(InputStream is, String streamName, FileWriter fileOs, StringBuilder outputBuffer, boolean consumeFully) {
        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(is);
            int len = 1024;
            byte[] b = new byte[len];
            for (;;) {
                int ret = bufferedInputStream.read(b, 0, len);
                if (ret == -1)
                    break;
                String grab = new String(b, 0, ret);
                logger.log(Level.INFO, streamName + ": " + grab);
                if (outputBuffer != null) {
                    outputBuffer.append(grab);
                }
                if (fileOs != null) {
                    fileOs.write(grab);
                }

                if (!consumeFully && is.available() == 0) {
                    return;
                }
            }
        } catch (IOException ioe) {
            logger.warn("An exception occurred while processing child process stream " + streamName, ioe);
        } finally {
            try {
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (fileOs != null) {
                    fileOs.close();
                }
            } catch (IOException e) {
            }

        }
    }

	/**
	 * A specification for heap size of a JVM.
	 */
	public static class HeapSpec {
		private final int minHeapMB;
		private final int maxHeapMB;

		public HeapSpec(int minHeapMB, int maxHeapMB) {
			this.minHeapMB = minHeapMB;
			this.maxHeapMB = maxHeapMB;
		}

		/**
		 * Returns the minimum heap size in MB.
		 */
		public int getMinHeapMB() {
			return this.minHeapMB;
		}

		/**
		 * Returns the maximum heap size in MB.
		 */
		public int getMaxHeapMB() {
			return this.maxHeapMB;
		}

		/**
		 * Returns the String that can be used in the execution of the java command.
		 */
		public String getJavaCmdString() {
			String minStr = this.minHeapMB > 0 ? "-Xms" + this.minHeapMB + "m" : "";
			if (this.minHeapMB > 0 && this.maxHeapMB > 0) {
				minStr += " ";
			}
			String maxStr = this.maxHeapMB > 0 ? "-Xmx" + this.maxHeapMB + "m" : "";
			return minStr + maxStr;
		}
	}

	public static final String ARG_PROG_ARGS = "progArgs=";
	public static final String ARG_JUNIT_CLASSES = "junitClasses=";
	public static final String ARG_WAIT_TIME_BEFORE_KILL = "waitTimeBeforeKill=";
	public static final String ARG_HEAP_SPECS = "heapSpecs=";
	public static final String ARG_SETUP_PROG_ARGS = "setupProgArgs=";

	private List<StreamReaderThread> ioThreads = new ArrayList<>();

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {
		try {
			if (args.length == 0) {
				System.out.println(getUsage());
				System.exit(1);
			}

			List<String[]> progArgs = new ArrayList<>();
			List<String> junitClasses = null;
			HeapSpec[] heapSpecs = null;
			Long waitTimeBeforeKill = null;
			String[] setupProgArgs = null;
			HeapSpec setupProgHeapSpec = null;

			for (int i = 0; i < args.length; i++) {
				String arg = args[i];
				if (arg.startsWith(ARG_PROG_ARGS)) {
					arg = arg.replace(ARG_PROG_ARGS, "");
					String[] split = arg.split("\\|");
					for (String progArg : split) {
						String[] progArgSplit = progArg.split("\\s");
						progArgs.add(progArgSplit);
					}

				} else if (arg.startsWith(ARG_HEAP_SPECS)) {
					arg = arg.replace(ARG_HEAP_SPECS, "");
					String[] split = arg.split("\\|");
					heapSpecs = new HeapSpec[split.length];
					for (int j = 0; j < split.length; j++) {
						String specStr = split[j];
						HeapSpec heapSpec = getHeapSpecFromInput(specStr);
						heapSpecs[j] = heapSpec;
					}

				} else if (arg.startsWith(ARG_WAIT_TIME_BEFORE_KILL)) {
					arg = arg.replace(ARG_WAIT_TIME_BEFORE_KILL, "");
					waitTimeBeforeKill = Long.parseLong(arg);

				} else if (arg.startsWith(ARG_JUNIT_CLASSES)) {
					arg = arg.replace(ARG_JUNIT_CLASSES, "");
					String[] classNames = arg.split(",");
					junitClasses = new ArrayList<>();
					for (String className : classNames) {
						junitClasses.add(className);
					}

				} else if (arg.startsWith(ARG_SETUP_PROG_ARGS)) {
					arg = arg.replace(ARG_SETUP_PROG_ARGS, "");
					if (arg.contains("|")) {
						String[] split = arg.split("\\|");
						String setupArgs = split[0];
						setupProgArgs = setupArgs.split("\\s");

						String heapSpecStr = split[1];
						setupProgHeapSpec = getHeapSpecFromInput(heapSpecStr);

					} else {
						String[] split = arg.split("\\s");
						setupProgArgs = split;
					}

				} else {
					System.out.println("Invalid argument");
					System.out.println(getUsage());
					System.exit(1);
				}
			}

			AppForker runner = new AppForker(progArgs);
			if (heapSpecs != null) {
				runner.setHeapSpecs(heapSpecs);
			}
			if (waitTimeBeforeKill != null) {
				runner.setWaitTimeBeforeKill(waitTimeBeforeKill);
			}
			if (junitClasses != null) {
				runner.setJunitClasses(junitClasses);
			}
			if (setupProgArgs != null) {
				runner.setSetupProgramArgs(setupProgArgs);
			}
			if (setupProgHeapSpec != null) {
				runner.setSetupProgramHeapSpec(setupProgHeapSpec);
			}

			runner.execute();
			runner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static HeapSpec getHeapSpecFromInput(String specStr) {
		String[] specParts = specStr.split(",");
		int minHeapMB = Integer.parseInt(specParts[0]);
		int maxHeapMB = Integer.parseInt(specParts[1]);
		HeapSpec heapSpec = new HeapSpec(minHeapMB, maxHeapMB);
		return heapSpec;
	}

	public static String getUsage() {
		String className = AppForker.class.getSimpleName();
		return className + " " + ARG_PROG_ARGS + "<program arguments, separate programs with |> \n"
				+ ARG_HEAP_SPECS + "[optional heap specifications in the form MinSizeMB,MaxSizeMB, separate JVMs with | or use one for all] \n"
				+ ARG_WAIT_TIME_BEFORE_KILL + "[optional wait time in ms before killing JVMs] \n"
				+ ARG_JUNIT_CLASSES + "[use this to specify a list of JUnit test classes separated by comma, each will run in a separate JVM] \n"
				+ ARG_SETUP_PROG_ARGS + "[optional setup program to run before running the programs specified in progArgs. Heap spec can follow args after a |] \n"

				+ "\nExample: Run the same program in two JVMs...\n"
				+ className + " progArgs=\"com.valencia.test.SimpleTestProgram 10000|com.valencia.test.SimpleTestProgram 10000\" heapSpecs=\"0,256|128,256\""
				+ "\n\nExample: Run a JUnit...\n"
				+ className + " junitClasses=\"com.valencia.MyTest\" heapSpecs=\"0,256\"";
	}

}
