
int x;
int y;
int z;

int main();
void thread1();
void thread3();
void thread2();


int main() {
  x = 0;
  y = 0;
  z = 0;
  thread1();
  thread2();
  thread3();
}

void thread1() {
  x = 1;
  z = 1;
  y = 1;
}

void thread3() {
  assume(z == 1);
  assert(y == 1);
}

void thread2() {
  assume(x == 1);
  assume(y == 1);
  assert(z == 1);
}

