
 int registered = 0;
 int initialised1 = 0;
 int initialised2 = 0;
 int initialised3 = 0;


srp_attach_transport()
{
    initialised1 = 1;
    initialised3 = 1; // must stay below the above two lines
    initialised2 = 1; //put this to top (because of thread2)
    registered = 1;
}

thread1()
{
    srp_attach_transport();
}

thread2()
{
    assume(registered == 1);
    assert (initialised2 == 1);
}

thread3()
{
    assume (initialised3 == 1);
    assert (registered == 1);
    assert (initialised1 == 1);
}


void main() {
    registered = 0;
    initialised1 = 0;
    initialised2 = 0;
    initialised3 = 0;
	thread1();
	thread2();
    thread3();
}