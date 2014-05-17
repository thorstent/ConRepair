#define CONFIG_THREAD 1
#define START_THREAD  2

int lock1;
int rtnl_lock;
int want_lock;
int want_rtnl_lock;
int restarted;
int notify;

void main();
int reassoc_thread();
int config_thread();
int iwl3945_bg_alive_start_thread();


void main() {
  lock1 = 0;
  want_lock = 0;
  rtnl_lock = 0;
  want_rtnl_lock = 0;
  restarted = 0;
  notify = 0;
  config_thread();
  iwl3945_bg_alive_start_thread();
  reassoc_thread();
}

int reassoc_thread() {
  assume(notify == 1);
  assert(restarted == 1);
}

int config_thread() {
  noReorderBegin();
    atomicBegin();
      assume(rtnl_lock == 0);
      rtnl_lock = 1;
    atomicEnd();
    want_lock = 1;
    assert((lock1 == 0) | (want_rtnl_lock != 2));
    atomicBegin();
      assume(lock1 == 0);
      lock1 = 1;
      want_lock = 0;
    atomicEnd();
    lock1 = 0;
    rtnl_lock = 0;
  noReorderEnd();
}

int iwl3945_bg_alive_start_thread() {
  atomicBegin();
    assume(lock1 == 0);
    lock1 = 1;
  atomicEnd();
  restarted = 1;
  lock1 = 0;
  noReorderBegin();
    want_rtnl_lock = 2;
    atomicBegin();
      assume(rtnl_lock == 0);
      rtnl_lock = 1;
      want_rtnl_lock = 0;
    atomicEnd();
    notify = 1;
    rtnl_lock = 0;
  noReorderEnd();
}

