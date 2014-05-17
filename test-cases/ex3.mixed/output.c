                                         unsigned int nondet_int();
                                         int __assume_dummy = 0;



                                         int registered = 0;
                                         int initialised1 = 0;
                                         int initialised2 = 0;
                                         int initialised3 = 0;

                                         void main();
                                         int thread1();
                                         int thread3();
                                         int thread2();
                                         int srp_attach_transport();




                                         void main() {
__CPROVER_atomic_begin();                  registered = 0;                                                        __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  initialised1 = 0;                                                      __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  initialised2 = 0;                                                      __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  initialised3 = 0;                                                      __CPROVER_atomic_end();
                                           __CPROVER_ASYNC_1: thread1();
                                           __CPROVER_ASYNC_1: thread2();
                                           __CPROVER_ASYNC_1: thread3();
                                            }

                                         int thread1() {
                                           srp_attach_transport();
                                         }

                                         int thread3() {
__CPROVER_atomic_begin();                  __CPROVER_assume(initialised3 == 1); __assume_dummy=0;                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(registered == 1);                                               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(initialised1 == 1);                                             __CPROVER_atomic_end();
                                         }

                                         int thread2() {
__CPROVER_atomic_begin();                  __CPROVER_assume(registered == 1); __assume_dummy=0;                   __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(initialised2 == 1);                                             __CPROVER_atomic_end();
                                         }

                                         int srp_attach_transport() {
__CPROVER_atomic_begin();                  initialised1 = 1;                                                      __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  initialised2 = 1;                                                      __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  registered = 1;                                                        __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  initialised3 = 1;                                                      __CPROVER_atomic_end();
                                         }


// Line: 53