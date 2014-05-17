
int registered = 0;
int initialised1 = 0;
int initialised2 = 0;
int initialised3 = 0;

void main();
int thread1();
int thread3();
int thread2();
int srp_attach_transport();


void main() {
  registered = 0;
  initialised1 = 0;
  initialised2 = 0;
  initialised3 = 0;
  thread1();
  thread2();
  thread3();
}

int thread1() {
  srp_attach_transport();
}

int thread3() {
  assume(initialised3 == 1);
  assert(registered == 1);
  assert(initialised1 == 1);
}

int thread2() {
  assume(registered == 1);
  assert(initialised2 == 1);
}

int srp_attach_transport() {
  initialised1 = 1;
  initialised3 = 1;
  initialised2 = 1;
  registered = 1;
}

