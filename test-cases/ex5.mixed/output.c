                                         unsigned int nondet_int();
                                         int __assume_dummy = 0;



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
__CPROVER_atomic_begin();                  x = 0;                                                                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  y = 0;                                                                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  z = 0;                                                                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  a = 0;                                                                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  b = 0;                                                                 __CPROVER_atomic_end();
                                           __CPROVER_ASYNC_1: thread1();
                                           __CPROVER_ASYNC_1: thread2();
                                           __CPROVER_ASYNC_1: thread3();
                                           __CPROVER_ASYNC_1: thread4();
                                            }

                                         int thread1() {
__CPROVER_atomic_begin();                  x = 1;                                                                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  y = 1;                                                                 __CPROVER_atomic_end();
                                         }

                                         int thread3() {
__CPROVER_atomic_begin();                  __CPROVER_assume(b == 1); __assume_dummy=0;                            __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  x = 2;                                                                 __CPROVER_atomic_end();
                                         }

                                         int thread2() {
__CPROVER_atomic_begin();                  __CPROVER_assume(y == 1); __assume_dummy=0;                            __CPROVER_atomic_end();
                                           __CPROVER_atomic_begin();
                                             //noReorderBegin();
                                               assert(x == 1);                                                    
                                               a = 1;                                                             
                                             //noReorderBegin();
                                             b = 1;                                                               
                                           __CPROVER_atomic_end();
                                         }

                                         int thread4() {
__CPROVER_atomic_begin();                  __CPROVER_assume(a == 1); __assume_dummy=0;                            __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(b == 1);                                                        __CPROVER_atomic_end();
                                         }


// Line: 60