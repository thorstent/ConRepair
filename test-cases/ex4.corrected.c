#define CLIENT1 1
#define CLIENT2 2
#define WORKER  3

int lock1;
int bsy;
int pending;
int request;

void main();
int acm_cdc_notify(int thread_id);
int worker_thread();
int client_thread1();
int client_thread2();


void main() {
  lock1 = 0;
  bsy = 0;
  pending = 0;
  request = 0;
  client_thread1();
  client_thread2();
  worker_thread();
}

int acm_cdc_notify(int thread_id) {
  int result;
  result = 0;
  atomicBegin();
    assume(lock1 == 0);
    lock1 = thread_id;
  atomicEnd();
  pending = 0;
  if (bsy == 0) {
    bsy = 1;
    atomicBegin();
      lock1 = 0;
      if (nondet == 0) {
        result = -1;
      } else {
        request = thread_id;
      }
      if (result == (-1)) {
        bsy = 0;
      }
    atomicEnd();
  } else {
    pending = 1;
    assert(bsy);
    lock1 = 0;
  }
}

int worker_thread() {
  while (1) {
    assume(request);
    assert(lock1 != request);
    atomicBegin();
      assume(lock1 == 0);
      lock1 = 3;
    atomicEnd();
    bsy = 0;
    request = 0;
    lock1 = 0;
    if (pending == 0) {
    } else {
      acm_cdc_notify(3);
    }
  }
}

int client_thread1() {
  acm_cdc_notify(1);
}

int client_thread2() {
  acm_cdc_notify(2);
}

