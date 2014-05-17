ConRepair
=========
ConRepair is the prototype implementation accompanying the paper submitted at CAV2014.

CAV2014 tag
-----------
This is the CAV2014 tag, the official version submitted to CAV2014. 

Folders
=======
This is the content of the subfolders. Each has its own readme file explaining the content in detail.

test-cases
----------
These contain the test-cases and log files produced by ConRepair after it ran on the c files.

binary-release
--------------
Contains the JAR file (java binary) as part of the CAV submission.

implementation
--------------
This folder contains sources.

Differences to the paper
========================
* the await command is called assume in the examples. All assumes are waiting assumes.
* The non-waiting assume is modelled with if-conditions

Manual
======

Command line options
--------------------

This is a short manual on how to run the tool. The tool has a number of command line options; for a full list see the --help option of the binary.

| Commandline option | Explanation |
| --- | --- |
| `--cache` | will cache the CBMC output in the ioCache subfolder of the current folder. This speeds up later verifications of the same file. This flag will not influence the timing results because the time CBMC took is also cached. |
| `-m` | Has a number of options to influence the search strategy. `verify` will only check if the current code is correct. `badOnly` will do the analysis without looking at good traces. `mixed` is the default and learns from good traces before fixing the program. `combined` will test both approaches and print out a combined statistic (the one that is found in the test-cases folder). `semaphore` will place semaphores and compare this with the badOnly mode. This mode will only place semaphores and then test if the program is now secure, whereas mixed mode will *never* place semaphores. |
| `-t <number>` | The number of threads that can be spawned in parallel for the good trace analysis |
| `-a` | Will allow the badOnly algorithm to shorted backtracking and instead insert atomic sections. Will result in shorter run but more atomic sections |

Commands on std-in
------------------

The implementation can process commands on std-in while it is running. Currently the only command is `cancel`. If you type this followed by a return it will cancel the run as soon as the current iteration is complete. This is more graceful than Ctrl+C, because it actually outputs a statistic.

Phases of the verification and the non-deadlock mode
----------------------------------------------------

Depending on what we wish to know we output different assertions in the code we pass to CBMC. There are two modes for good traces and three for bad traces

### Complete program trace
This is always the first iteration of the good trace learning phase. At the end of the code an assert(false) is inserted and we ensure all threads run to completion. All asserts are changed to assumes to ensure the trace is good. If there is no such trace a warning is printed as this is probably not intended by the programmer that the program has no good trace.

### Good trace to an assertion
This mode prints an assert(false) after an assertion turned into an assumption. This will give us a trace where one specific assertion succeeds. After that a second trace is sought where the assertion reads from a different assumption.

### Bad trace finding
For every failing assertion a counter is increased and in the end we place an assert(counter==0). If this gives us a counter-example we get a complete trace through the program that can be analysed. However, if this does not give a counter-example this does not mean the program is correct. It could be the case that after the bug the program cannot be executed to completion.

### Verification
Because non-deadlock mode is so expensive the verification step can quickly verify the program. It leaves all asserts where they were originally. This produces no suitable trace, but it can quickly check if the program is correct or not.

### Non-deadlock mode
After the bad-trace finding returns no more bad traces we do a verification step and if that fails enter non-deadlock mode. In this mode every assumption after the error may simply pass. So every assume(x) is replaced by assume(counter>0||x). This means the trace may be wrong after the assertion failed, but at least we get a trace. Since non-deadlock mode takes forever once the program is correct a verification step needs to be inserted after every run.

Global variables
----------------
All global variables need to be initialised in main(). Otherwise you may get an exeception when learning from good traces.

main()
------

The main function must only initialised **all** variables and then call all the threads. Threads not called here will not be spawend.

threads
-------
All functions that represent threads must have "thread" in their name. They must be called from main and they must not be called from any other place. A thread that only exists to detect deadlocks must have "deadlock" in its name.

Well-known functions
--------------------
The following functions can be used the C code. They are not to be declared in the header. **These words cannot be used as variable names.**

| Command | Explanation |
| --- | --- |
| `beginAtomic()`, `endAtomic()` | Starts and ends an atomic section. These are to be put around a block of code that is meant to be atomic. They cannot be placed around a function call. |
| `beginNoReorder()`, `endNoReorder()` | This block of statements cannot be reordered. But the block as a whole may move. |
| `lock(int)`, `unlock(int)` | Lock or unlock a lock. The argument must be a global variable. These locks are not tested for deadlocks. This needs to be modelled manually. |
| `nondet` | This is a variable that can be used to construct a non-deterministic if or while loop. |
| `nondet_int()` | This function can be called to get a non-deterministic integer value. |
| `assert(int)` | Assert the argument is !=0 |
| `assume(int)` | This is a waiting assume. The thread will continue once the assumption is true. |
| `assertd(int)` | Assert that is especially for deadlocks. The variables used in this assert can be reordered around it. |
| `asserta` | Used for testing (interal use only). |

### Examples

	void example1() {
		beginAtomic();
		x = 1;
		y = 1;
		endAtomic();
	}

	int test_lock = 0;
	void example2() {
		lock(test_lock);
		unlock(test_lock);
	}

	void example3() {
		if (nondet==1) then {} else {}
	}

	int example4() {
		int x = nondet_int();
		return x;
    }

Limitations of the C input code
-------------------------------

* Conditions: No usage of && or ||, use & or | instead (because of the way CPAchecker handles these)
* Nondet: nondet can be used as a variable in *if* and *while* loops. It is a variable that does not need to be declared, it is not a function.
* Explicit comparing: while(nondet) is not good. Instead write while(nondet==1)
* Return statements: There can be at most one return statement in the function, this must be the last instruction and not inside an if or while.
* Variable naming: All variable names must be globally unique.
* `continue` and `break` must not be used.

Function naming convention
--------------------------

Functions that have thread in their name spawn new threads when called. Such a function may only be called from main.
The function deadlock_thread only serves to detect deadlocks (so its condition can scheduled whenever a deadlock occurs). The deadlock detection needs to be modelled manually.

