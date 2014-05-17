                                         unsigned int nondet_int();
                                         int __assume_dummy = 0;



                                         int starting = 0;
                                         int IntrMask = 0;
                                         int patched = 0;

                                         void main();
                                         int wm5102_patch();
                                         void arizona_dev_init();
                                         int thread1();
                                         int thread2();
                                         int set_IntrMask();
                                         int arizona_wait_for_boot();


                                         int __thread1_finished = 0;
                                         int __thread2_finished = 0;


                                         void main() {
__CPROVER_atomic_begin();                  starting = 1;                                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  IntrMask = 0;                                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  patched = 0;                                                           __CPROVER_atomic_end();
                                           __CPROVER_ASYNC_1: thread1();
                                           __CPROVER_ASYNC_1: thread2();
                                           __CPROVER_assume(__thread1_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__thread2_finished == 1); __assume_dummy=0;
                                           assert(0);
                                         }

                                         int wm5102_patch() {
__CPROVER_atomic_begin();                  assert(starting == 0);                                                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  patched = 1;                                                           __CPROVER_atomic_end();
                                         }

                                         void arizona_dev_init() {
                                           arizona_wait_for_boot();
                                           wm5102_patch();
                                           set_IntrMask();
                                         }

                                         int thread1() {
                                           arizona_dev_init();
                                           __thread1_finished = 1;
                                         }

                                         int thread2() {
__CPROVER_atomic_begin();                  starting = 0;                                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  __CPROVER_assume(IntrMask == 1); __assume_dummy=0;                     __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(patched == 1);                                                  __CPROVER_atomic_end();
                                           __thread2_finished = 1;
                                         }

                                         int set_IntrMask() {
                                           arizona_wait_for_boot();
__CPROVER_atomic_begin();                  IntrMask = 1;                                                          __CPROVER_atomic_end();
                                         }

                                         int arizona_wait_for_boot() {
__CPROVER_atomic_begin();                  __CPROVER_assume(starting == 0); __assume_dummy=0;                     __CPROVER_atomic_end();
                                         }


// Line: 67