                                         unsigned int nondet_int();
                                         int __assume_dummy = 0;



                                         int napi_poll = 0;
                                         int shutdown = 0;
                                         int stuff1_done = 0;
                                         int updated = 0;
                                         int napi_disabled = 0;

                                         void main();
                                         int disable_napi();
                                         void rtl_shutdown();
                                         void rtl8169_poll();
                                         void thread1();
                                         void thread3();
                                         void thread2();
                                         int update_state();
                                         void stuff3();
                                         void stuff2();
                                         void stuff1();




                                         void main() {
__CPROVER_atomic_begin();                  napi_poll = 0;                                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  shutdown = 0;                                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  stuff1_done = 0;                                                       __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  updated = 0;                                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  napi_disabled = 0;                                                     __CPROVER_atomic_end();
                                           __CPROVER_ASYNC_1: thread1();
                                           __CPROVER_ASYNC_1: thread2();
                                           __CPROVER_ASYNC_1: thread3();
                                            }

                                         int disable_napi() {
__CPROVER_atomic_begin();
                                           __CPROVER_assume(napi_poll == 0); __assume_dummy=0;                    
                                           napi_poll = 1;                                                         
                                                                                                                  __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  napi_disabled = 1;                                                     __CPROVER_atomic_end();
                                         }

                                         void rtl_shutdown() {
                                           stuff2();
                                         }

                                         void rtl8169_poll() {
                                           stuff3();
                                         }

                                         void thread1() {
                                           update_state();
                                           disable_napi();
                                           stuff1();
                                         }

                                         void thread3() {
__CPROVER_atomic_begin();
                                           __CPROVER_assume(napi_poll == 0); __assume_dummy=0;                    
                                           napi_poll = 1;                                                         
                                                                                                                  __CPROVER_atomic_end();
                                           rtl8169_poll();
__CPROVER_atomic_begin();
                                           napi_poll = 0;                                                         
                                                                                                                  __CPROVER_atomic_end();
                                         }

                                         void thread2() {
                                           rtl_shutdown();
                                         }

                                         int update_state() {
__CPROVER_atomic_begin();                  updated = 1;                                                           __CPROVER_atomic_end();
                                         }

                                         void stuff3() {
__CPROVER_atomic_begin();                  assert(shutdown == 0);                                                 __CPROVER_atomic_end();
                                         }

                                         void stuff2() {
__CPROVER_atomic_begin();                  __CPROVER_assume(napi_disabled == 1); __assume_dummy=0;                __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(updated == 1);                                                  __CPROVER_atomic_end();
                                         }

                                         void stuff1() {
__CPROVER_atomic_begin();                  shutdown = 1;                                                          __CPROVER_atomic_end();
                                         }


// Line: 93