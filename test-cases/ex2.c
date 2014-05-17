 
int starting = 0;
int IntrMask = 0;
int patched = 0;

 
arizona_wait_for_boot() {
    assume(starting == 0);
}

wm5102_patch() {
    assert(starting==0);
    patched = 1;
}

set_IntrMask() {
    arizona_wait_for_boot();
    IntrMask = 1;
}

void arizona_dev_init()
{
    wm5102_patch();
    set_IntrMask(); // this must stay below patch()
    arizona_wait_for_boot(); // move to top
}

thread1()
{
    arizona_dev_init();
}

thread2()
{
    // at some point we finish starting up
    starting = 0; // if we move this below there is the danger of a deadlock
    assume (IntrMask==1);
    assert (patched == 1);
}


void main() {
    starting = 1;
    IntrMask = 0;
    patched = 0;
	thread1();
	thread2();
}