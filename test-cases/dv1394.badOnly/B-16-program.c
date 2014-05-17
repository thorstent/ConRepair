#define MMAP_THREAD  1
#define IOCTL_THREAD 2
#define STATE0       0
#define INITIALISED  1
#define MAPPED       2
#define INCONSISTENT 3

int mtx;
int want_mtx;
int sem;
int want_sem;
int state;
int vm_consistent;

void main();
int mmap_thread();
int rw_thread();
int ioctl_thread();


void main() {
  mtx = 0;
  want_mtx = 0;
  sem = 1;
  want_sem = 0;
  state = 0;
  vm_consistent = 1;
  mmap_thread();
  ioctl_thread();
  rw_thread();
}

int mmap_thread() {
  atomicBegin();
    assume(sem == 1);
    sem = 0;
  atomicEnd();
  assert(vm_consistent == 1);
  sem = 1;
  noReorderBegin();
    assertd(((want_sem == 0) | (mtx == 0)) | (sem != 0));
    want_mtx = 1;
    atomicBegin();
      assume(mtx == 0);
      mtx = 1;
      want_mtx = 0;
    atomicEnd();
    assert(state != 3);
    state = 3;
    state = 1;
    state = 3;
    state = 2;
    mtx = 0;
  noReorderEnd();
  vm_consistent = 0;
  vm_consistent = 1;
}

int rw_thread() {
  atomicBegin();
    assume(mtx == 0);
    mtx = 1;
  atomicEnd();
  assert(state != 3);
  mtx = 0;
}

int ioctl_thread() {
  int old_state;
  old_state = state;
  state = 3;
  state = old_state;
  atomicBegin();
    assume(mtx == 0);
    mtx = 1;
  atomicEnd();
  if (nondet == 0) {
  } else {
    noReorderBegin();
      assert(((want_mtx == 0) | (sem == 1)) | (mtx == 0));
      want_sem = 2;
      atomicBegin();
        assume(sem == 1);
        sem = 0;
        want_sem = 0;
      atomicEnd();
      assert(vm_consistent);
      sem = 1;
    noReorderEnd();
  }
  mtx = 0;
  assertd(state != 3);
}

