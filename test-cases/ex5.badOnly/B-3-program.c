
int x;
int y;
int z;
int a;
int b;

void main();
int thread1();
int thread3();
int thread2();
int thread4();


void main() {
  x = 0;
  y = 0;
  z = 0;
  a = 0;
  b = 0;
  thread1();
  thread2();
  thread3();
  thread4();
}

int thread1() {
  x = 1;
  y = 1;
}

int thread3() {
  assume(b == 1);
  x = 2;
}

int thread2() {
  b = 1;
  noReorderBegin();
    assertd(x == 1);
    a = 1;
  noReorderEnd();
  assume(y == 1);
}

int thread4() {
  assume(a == 1);
  assert(b == 1);
}

