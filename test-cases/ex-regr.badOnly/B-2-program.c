
int x;
int y;

void main();
int thread1();
int thread3();
int thread2();


void main() {
  x = 0;
  y = 0;
  thread1();
  thread2();
  thread3();
}

int thread1() {
  y = 1;
  x = 1;
}

int thread3() {
  assert((x == 1) | (y == 0));
}

int thread2() {
  assume(x == 1);
  assert(y == 1);
}

