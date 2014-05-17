// fauty execution
// c1.0 (lock1:=CLIENT1) -> c1.1 -> c1.2 -> c1.3 -> c1.4 (lock1:=0) -> c1.5 -> c1.i -> c2.0 (lock1:=CLIENT2) -> c2.1 -> c2.2 (bsy==1) -> c2.7 -> c1.5 (bsy:=0) -> c2.8 (assertion failure)
//
// possible fix that eliminates this particular race: move statement 4 just after 6.  This creates another race:
// c1.0 (lock1:=CLIENT1) -> c1.1 -> c1.2 -> c1.3 -> c1.4 -> c1.i (request:=CLIENT1) -> w.A -> w.B (assertion failure)
//
// there does not exist a correct fix based solely on reordering.  One solution is to drop the lock1 before 4 and re-acquire it just before 5 and drop again after 5.
// Alternatively, just add an atomic section arond 4, 5, and 6.

// thread id's
#define CLIENT1 1
#define CLIENT2 2
#define WORKER  3

// ensures atomic access to other variables
int lock1; 

// bsy flag.  No new request can be started when this is true
int bsy;

// if a new request arrives when bsy==1 then this flag is set to 1;
// the pending request will be executed once the current request completes
int pending;

// signals to the worker thread that there is a request for it to handle
int request;

// Client thread 1
client_thread1() {
    acm_cdc_notify (CLIENT1);
}

// Client thread 2
client_thread2() {
    acm_cdc_notify (CLIENT2);
}


// Worker thread
worker_thread () {
    while (1) {
        // A.
        assume (request);

        // B. not allowed to wait here
        assert (lock1 != request);
        atomicBegin();
        assume (lock1 == 0);
        lock1 = WORKER;
        atomicEnd();

        // C. handle the request and update state variables
        bsy = 0;
        request = 0;

        // D.
        lock1 = 0;

        // E. if there are more requests pending, schedule them now
        if (pending)
            acm_cdc_notify(WORKER);
    };
}

// queue request
acm_cdc_notify (int thread_id) {
    int result = 0;
    // 0. acquire lock1
    atomicBegin();
    assume(lock1==0);
    lock1 = thread_id;
    atomicEnd();

    // 1. if we are invoked to handle a pending request, clear the pending flag.
    pending = 0;

    // 2.
    if (!bsy) {
        // 3. send request to worker thread
        bsy = 1;
        // 4.
        lock1 = 0;
        // 5.
        // attempt to submit a request to the worker thread; fail nondeterministically
        if (nondet) {
            // i.
            request = thread_id;
        } else {
            // ii.
            result = -1;
        }
        if (result == -1) 
            // 6.
            bsy = 0;


    } else {
        // 7. if the worker thread is busy, mark request as pending
        pending = 1;
        // 8. catch potential race: request has been marked as pending, but it will never be executed,
        //    as the worker thread has gone idle
        assert (bsy);

        // 9.
        lock1 = 0;
    };
}

void main () {
    lock1 = 0;
    bsy = 0;
    pending = 0;
    request = 0;

    client_thread1 ();
    client_thread2 ();
    worker_thread ();
}
