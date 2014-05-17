
int starting = 0;
int IntrMask = 0;
int patched = 0;

void main();
int wm5102_patch();
void arizona_dev_init();
int thread1();
int thread2();
int set_IntrMask();
int arizona_wait_for_boot();


void main() {
  starting = 1;
  IntrMask = 0;
  patched = 0;
  thread1();
  thread2();
}

int wm5102_patch() {
  assert(starting == 0);
  patched = 1;
}

void arizona_dev_init() {
  wm5102_patch();
  set_IntrMask();
  arizona_wait_for_boot();
}

int thread1() {
  arizona_dev_init();
}

int thread2() {
  atomicBegin();
    starting = 0;
    assume(IntrMask == 1);
  atomicEnd();
  assert(patched == 1);
}

int set_IntrMask() {
  arizona_wait_for_boot();
  IntrMask = 1;
}

int arizona_wait_for_boot() {
  assume(starting == 0);
}

