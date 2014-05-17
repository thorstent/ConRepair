int x;
int y;
int z;
int a;
int b;


thread1() {
    x = 1;
    y = 1;
}

thread2() {
    assume(y == 1);
    noReorderBegin();
    assertd(x == 1);
    a = 1;
    noReorderEnd();
    b = 1;
}

thread3() {
    assume(b == 1);
    x = 2;
}

thread4() {
    assume(a==1);
    assert(b==1);
}

void main () {
    x = 0;
    y = 0;
    z = 0;
    a = 0;
    b = 0;

    thread1 ();
    thread2 ();
    thread3 ();
    thread4 ();
}