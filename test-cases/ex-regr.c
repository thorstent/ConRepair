/*
 * The assertion is in some sense an implication, thereby it is an assume and an assert in one. This is completelly missed by the good trace analysis.
 */

int x;
int y;


thread1() {
    x = 1;
    y = 1;
}

thread2() {
    assume (x==1);
    assert (y==1);
}

thread3() {
    assert (x==1 | y==0);
}

void main () {
    x = 0;
    y = 0;
    
    thread1 ();
    thread2 ();
    thread3 ();
}