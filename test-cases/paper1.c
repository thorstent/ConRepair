// This is the simple example from Section 2 of the paper.

int x;
int y;
int z;

void thread1()
{
	x = 1;
	y = 1;
	z = 1;
}

void thread2()
{
	assume (x == 1);
	assume (y == 1);
	assert (z == 1);
}

void thread3()
{
	assume (z == 1);
	assert (y == 1);
}

main() {
    x = 0;
    y = 0;
    z = 0;
    thread1();
    thread2();
	thread3();
}
