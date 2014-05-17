
int napi_poll = 0;
int shutdown = 0;
int stuff1_done = 0;
int updated = 0;
int napi_disabled = 0;

void main();
int disable_napi();
void rtl_shutdown();
void rtl8169_poll();
void thread1();
void thread3();
void thread2();
int update_state();
void stuff3();
void stuff2();
void stuff1();


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

int disable_napi() {
  lock(napi_poll);
  napi_disabled = 1;
}

void rtl_shutdown() {
  stuff2();
}

void rtl8169_poll() {
  stuff3();
}

void thread1() {
  update_state();
  disable_napi();
  stuff1();
}

void thread3() {
  lock(napi_poll);
  rtl8169_poll();
  unlock(napi_poll);
}

void thread2() {
  rtl_shutdown();
}

int update_state() {
  updated = 1;
}

void stuff3() {
  assert(shutdown == 0);
}

void stuff2() {
  assume(napi_disabled == 1);
  assert(updated == 1);
}

void stuff1() {
  shutdown = 1;
}

