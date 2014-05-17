/* Based on 8449fc3ae58bf8ee5acbd2280754cde67b5db128
 *
 * Race in the initial implementation (ABBA deadlock): 1 -> 2 -> 3 -> 4 -> 5 (want_mtx:=MMAP_THREAD) -> A -> B -> C -> D -> E (assertion failure)
 * Possible fix: Move J after D. 
 * New race: A -> B -> C -> D -> J -> i -> ii (assertion failure)
 *
 * A valid fix would be to place 14 after 3, but one would have to move 13 as well or assertion G may get triggered.
 */

int mtx;
int want_mtx;
int sem;
int want_sem;

#define MMAP_THREAD  1
#define IOCTL_THREAD 2

#define STATE0       0
#define INITIALISED  1
#define MAPPED       2
#define INCONSISTENT 3

int state;
int vm_consistent;

mmap_thread () {

    // 1. lock semaphore
    atomicBegin();
    assume(sem == 1);
    sem = 0;
    atomicEnd();

    // 2.
    assert (vm_consistent == 1);
    // 3.
    vm_consistent = 0;

    // This should hopefully prevent the tool from moving statements
    // across the boundaries of the if-block.
    noReorderBegin();
        // 4.
        assertd ((want_sem == 0) | (mtx == 0) | (sem!=0));
        // 5.
        want_mtx = MMAP_THREAD;

        // 6. lock mutex
        atomicBegin();
        assume (mtx==0);
        mtx = 1;
        want_mtx = 0;
        atomicEnd();

        // 7.
        assert (state != INCONSISTENT);
        // 8.
        state = INCONSISTENT;
        // 9.
        state = INITIALISED;
        // 10.
        state = INCONSISTENT;
        // 11.
        state = MAPPED;

        // 12.
        mtx = 0;
    noReorderEnd();

    // 13.
    vm_consistent = 1;
    // 14.
    sem = 1;
}

ioctl_thread () {
    int old_state;

    // A.
    atomicBegin();
    assume(mtx == 0);
    mtx = 1;
    atomicEnd();

    // B.
    assertd (state != INCONSISTENT);
    // C.
    old_state = state;
    // D.
    state = INCONSISTENT;

    if (nondet) {
        // page fault: external code that cannot be reordered
        noReorderBegin();
 
        // E.
        assert ((want_mtx == 0) | (sem == 1) | (mtx==0));
        want_sem = IOCTL_THREAD;
        
        // F.
        atomicBegin();
        assume(sem == 1);
        sem = 0;
        want_sem = 0;
        atomicEnd();

        // G.
        assert (vm_consistent);

        // H.
        sem = 1;
        noReorderEnd();
    }

    // I.
    state = old_state;
    // J.
    mtx = 0;
}

rw_thread () {
    // i.
    atomicBegin();
    assume(mtx == 0);
    mtx = 1;
    atomicEnd();

    // ii.
    assert (state != INCONSISTENT);

    // iii.
    mtx = 0;
}

void main () {

    mtx = 0;
    want_mtx = 0;
    sem = 1;
    want_sem = 0;
    state = STATE0;
    vm_consistent = 1;

    mmap_thread ();
    ioctl_thread ();
    rw_thread ();
}
