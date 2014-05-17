                                         unsigned int nondet_int();
                                         int __assume_dummy = 0;



                                         int x;
                                         int y;
                                         int z;

                                         int main();
                                         void thread1();
                                         void thread3();
                                         void thread2();




                                         int main() {
__CPROVER_atomic_begin();                  x = 0;                                                                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  y = 0;                                                                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  z = 0;                                                                 __CPROVER_atomic_end();
                                           __CPROVER_ASYNC_1: thread1();
                                           __CPROVER_ASYNC_1: thread2();
                                           __CPROVER_ASYNC_1: thread3();
                                            }

                                         void thread1() {
__CPROVER_atomic_begin();                  y = 1;                                                                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  z = 1;                                                                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  x = 1;                                                                 __CPROVER_atomic_end();
                                         }

                                         void thread3() {
__CPROVER_atomic_begin();                  __CPROVER_assume(z == 1); __assume_dummy=0;                            __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(y == 1);                                                        __CPROVER_atomic_end();
                                         }

                                         void thread2() {
__CPROVER_atomic_begin();                  __CPROVER_assume(x == 1); __assume_dummy=0;                            __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  __CPROVER_assume(y == 1); __assume_dummy=0;                            __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(z == 1);                                                        __CPROVER_atomic_end();
                                         }


// Line: 45