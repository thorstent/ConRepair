/* This example from the r8169 driver illustrates the REORDER.LOCK pattern.

 The example consists of three threads.*/

int napi_poll = 0;
int shutdown = 0;
int stuff1_done = 0;
int updated = 0;
int napi_disabled = 0;

void stuff1();
void stuff2();
void stuff3();

/*Thread 1 (Interface down thread)
==================================*/

int update_state()
{
    updated = 1;
}

int disable_napi() {
    lock(napi_poll);
    napi_disabled = 1;
}

// driver entry point
void thread1()
{
/*(1)*/ stuff1(); // should be moved to bottom
        update_state();
/*(2)*/ disable_napi(); // disable NAPI loop
}

void stuff1() {
	shutdown = 1;
}

/*Thread 2 (Shutdown thread)
===========================*/

// OS model
void thread2()
{
    //while (stuff1_done == 0){};
    
    rtl_shutdown();
}

// driver entry point
void rtl_shutdown()
{
	stuff2();
}


void stuff2() {
    // make sure we don't get into a situation where the loop is disabled, but 
    // the values are not updated yet
	assume (napi_disabled==1);
    assert (updated==1);
	//assert (! ((napi_disabled==1) && (updated==0)));
	
}

/*Thread 3 (NAPI thread)
======================*/

// OS model
void thread3()
{
	lock(napi_poll);
	rtl8169_poll();
	unlock(napi_poll);
}

// driver entry point
void rtl8169_poll()
{
	stuff3();
}

void stuff3() {
	assert(shutdown == 0);
}

void main() {
	napi_poll = 0;
	shutdown = 0;
    stuff1_done = 0;
    updated = 0;
    napi_disabled = 0;
	thread1();
	thread2();
	thread3();
}

/*
The above code contains calls to functions stuff1(), stuff2(), 
stuff3().  These functions represent device-specific code that 
accesses device registers.  Since we do not have an accurate 
device model, we have to assume that these functions are not 
commutative and are only safe to execute in the order consistent 
with sequential execution.

Consider lines (1) and (2).  (2) disables the NAPI thread: 
after this lines has been executed, no new calls to the 
rtl8169_poll method will be performed.  In the simplified 
concurrency model the rtl8169_poll entry point cannot be invoked 
while rtl8169_down is running.  As a result, this model rules out 
any execution where stuff3() is called after stuff1().  

In the realistic concurrency model, a call to rtl8169_poll can 
occur between (1) and (2), thus violating the above invariant.  
This race can be eliminated by simply reordering lines (1) and 
(2), which is exactly what the RTL driver does.
*/
