/* Based on d54bc4e3fc5c56600a13c9ebc0a7e1077ac05d59
 * 
 * Initial race is an ABBA deadlock: A(lock:=1)->B->C(want_rtnl_lock := START_THREAD)->1->2->3 (assertion failure)
 * The simplest reordering that eliminates this race is to move the block of statements CDEFG before A.
 * This creates a new race: C->D->E->F(notify:=1)->i->ii (assertion failure)
 *
 * Correct reordering: ABH{CDEFG}
 */

#define CONFIG_THREAD 1
#define START_THREAD  2

// Two locks involved in ABBA deadlock
int lock1;
int rtnl_lock;

// Aux variables used to check for the deadlock
int want_lock;
int want_rtnl_lock;

int restarted;
int notify;

config_thread () {
    noReorderBegin ();
    
    // 1.
    {
        atomicBegin();
        assume(rtnl_lock == 0);
        rtnl_lock = 1;
        atomicEnd ();
    };
    
    // 2.
    want_lock = CONFIG_THREAD;
    // 3. check for ABBA deadlock
    assert((lock1 == 0) | (want_rtnl_lock != START_THREAD));
    
    // 4.
    {
        atomicBegin ();
        assume (lock1 == 0);
        lock1 = 1;
        want_lock = 0;
        atomicEnd();
    };
    
    // This thread is only needed to model ABBA deadlock,
    // we don't model what it actually does.
    
    // 5.
    lock1 = 0;
    // 6.
    rtnl_lock = 0;
    
    noReorderEnd ();
}

iwl3945_bg_alive_start_thread () {
    // A.
    {
        atomicBegin();
        assume(lock1==0);
        lock1 = 1;
        atomicEnd();
    };
    
    // B.
    restarted = 1;
    
    noReorderBegin();
    // C.
    want_rtnl_lock = START_THREAD;
    // D. check for ABBA deadlock
    // only one check is needed for the deadlock to be detected
    //assertd((rtnl_lock == 0) | (want_lock != CONFIG_THREAD));
    
    // E.
    {
        atomicBegin();
        assume(rtnl_lock == 0);
        rtnl_lock = 1;
        want_rtnl_lock = 0;
        atomicEnd();
    };
    
    // F. tell reassoc_thread that we are ready
    notify = 1;
    
    // G.
    rtnl_lock = 0;
    noReorderEnd();
    
    // H.
    lock1 = 0;
}

reassoc_thread () {
    // i.
    assume (notify == 1);
    // ii.
    assert (restarted == 1);    
}

void main () {
    lock1 = 0;
    want_lock = 0;
    rtnl_lock = 0;
    want_rtnl_lock = 0;
    restarted = 0;
    notify = 0;
    
    
    config_thread();
    iwl3945_bg_alive_start_thread ();
    reassoc_thread();
}