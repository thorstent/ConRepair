This folder contains the test-cases we tested our implementation on. There are two big examples that are models of real driver code, these are

* `rtl8169.c`: A skeleton of the realtek driver
* `usb-serial.c`: A skeleton of the USB framework in the Linux kernel

and a number of small ones that demonstrate different abilities of our implementation, described in detail in the paper.

The badOnly algorithm claims to find a solution for examples `ex2.c` and `lc-rc.c`, but the solution just produces a deadlock on closer inspection. The mixed algorithm finds the correct solution.

For each test-case a number of files are in this folder:

| File | Explanation |
| --- | --- |
| `.original.c` | The original program as printed out by the printer after parsing. This makes it easier to diff the file to the resulting file. |
| `.corrected.c` | The program after correcting all the errors. |
| `.statistic.txt` | The statistics file (log file of the run). |
| `.AS.statistic.txt` | The statistics file if the example is run with backtracing boundary and places atomic sections instead.  |

> All text files should be viewed with a monospace font to ensure tables look nice.

Statistic files
===============
For every test case a statistic file is created that compares the original approach (badOnly) to our improved approach (mixed).

At the top there are some general metrics (such as how many traces have been saved), such as the total number of rounds and wall-clock time. In parenthesis is the time spend in CBMC. For mixed mode a list of good traces follows. Their order can very from run to run because of concurrency in this phase.

Next follow the bad rounds. For each run there is a complete list of fixes applied. The list grows longer as more and more bugs are fixed. If it does not grow from one iteration to another this means the bug was not fixed and a different fix is tried (last line of the changes). A reordering may be tried twice because some reorderings may be applied in two different ways: Once the bottom statement is moved up before the top statement, the other time the top staptement is moved below the bottom statement.

In the middle or in the end there is a verification step. If this step is not successful this means there is a still a bug, but we cannot detect it if we insist on a complete trace. Then we switch to non-deadlock mode and obtain traces there. The non-deadlock mode is described in the manual (README.md of the parent folder).

Please note that CBMC/Z3 may give different results depending on a number of factors, which can result in different traces and thereby also in a different number of iterations because depending on the trace different reorderings may be tried first.

Subfolders
==========

The name is either {file}.mixed or {file}.badOnly, each for the corresponding run. These contain detailed files to analyse the program behaviour. .dot file can be open with graphviz installed. Graphviz can be installed on Ubuntu:

	sudo apt-get install graphviz

The .dot file can then be open from the terminal with

	xdot file.dot

> For all partial orders it is important that the arrows indicate a strict scheduled-after relationship, even if the elements are displayed side-by-side on the same vertical level. This is a problem with printing the subblocks inside the functions in DOT format.

| File | Explanation |
| --- | --- |
| `A-po.dot` | The partial order of the instructions in the program at the beginning. |
| `A-x-goodTrace.txt` | This textfile contains a good trace obtained from CBMC. x denotes the number of the trace. 1 is a general trace that just finishes the program. x.y means a trace that passes through assertion at line x and y indiciates the number of the trace for this assertion. This is relevant in case an assertion can read from more than one other location; then several traces are produced. |
| `A-x-proofObject.dot` | A graphical representation of the Proof Object. Here dotted black edges are the control flow through the program. Red edges are edges an assertion reads from. Blue edges are the edges an assumption reads from. Green edges edges that ensure an assertion actually reads from the red edge (no other write to the variable is scheduled in between). These are an overapproximation, because maybe such a scheduling would be impossible for other reasons. Solid black edges are the new edges we learned (to cover red or green edges); these instructions will not be reordered in later stages. Dotted red edges are suggestions we get for reorderings. The suggestions lead to later these reorderings being preferred if there are several options later. |
| `A-blackEdges.txt` | A list of black edges discovered from the Proof Objects sorted by Proof Object |
| `A-suggestions.txt` | A list of suggested reorderings found from the Proof Objects. They are tried later |
| `A-semaphores.txt` | Only present in semaphore mode. Indicates where semaphores should be placed. |
| `B-x-po.dot` | The partial order for this round. This incorporates any reorderings that are added for this round. x is the round counter |
| `B-x-program.c` | The c code for this iteration with all reorderings and atomic sections applied |
| `B-x-badTrace.c` | The trace that cause the error. The failing assertion is marked by !!. If several failed only the first failing assertion is attempted to be fixed. |
| `B-x-graph-begin.dot` | Shows the trace graphically with all the context-switches as black lines between the threads. This is a total order |
| `B-x-graph-gen.dot` | The trace generalised into a partial order. Some black edges are relaxed |
| `B-x-graph-sol-y.dot` | The proposed solution. The solution must form a cycle on the happens-before relation of the partial order. (the red edge identifies the solution, dotted edges are used to identify the edges needed to form a cycle) |
| `B-x-solutions.txt` | The solutions that will be tried next. They are not all correctly ordered yet, but they will be tried according to their "cost". The first columns is the round in which this solution was discovered. Then the type of solution and the cost as a tuple. The first element of the tuple is the round the solution was discovered in, the second element is the cost of the reordering or atomic section. Then follow the two lines that are to be reordered or put in an atomic section |
| `output.c` | The code that is actually passed to CBMC. This is overwritten every time CBMC is called. |
| `model-checker.log` | The output that CBMC created. Again only for the last successful call to CBMC. |

Reference machine
=================

All timings were collected on a Laptop with a i5-3320M processor with 4 cores and 8 GB RAM. For full parallelisation at least 2 GB RAM are needed per Z3 instance.