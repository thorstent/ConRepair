                                         unsigned int nondet_int();
                                         int __assume_dummy = 0;



                                         int x;
                                         int y;

                                         void main();
                                         int thread1();
                                         int thread3();
                                         int thread2();




                                         void main() {
__CPROVER_atomic_begin();                  x = 0;                                                                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  y = 0;                                                                 __CPROVER_atomic_end();
                                           __CPROVER_ASYNC_1: thread1();
                                           __CPROVER_ASYNC_1: thread2();
                                           __CPROVER_ASYNC_1: thread3();
                                            }

                                         int thread1() {
                                           __CPROVER_atomic_begin();
                                             x = 1;                                                               
                                             y = 1;                                                               
                                           __CPROVER_atomic_end();
                                         }

                                         int thread3() {
__CPROVER_atomic_begin();                  assert((x == 1) | (y == 0));                                           __CPROVER_atomic_end();
                                         }

                                         int thread2() {
__CPROVER_atomic_begin();                  __CPROVER_assume(x == 1); __assume_dummy=0;                            __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(y == 1);                                                        __CPROVER_atomic_end();
                                         }


// Line: 42