int x;

void thread1()
{
	x = 1;
}

void thread2()
{
	assert (x == 1);
}

void thread3()
{
	x = 1;
}

void thread4()
{
	x = 1;
}

main() {
    x = 0;
    thread1();
    thread2();
	thread3();
	thread4();
}
