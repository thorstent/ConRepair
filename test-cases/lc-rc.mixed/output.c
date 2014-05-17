                                         unsigned int nondet_int();
                                         int __assume_dummy = 0;



                                         int neh_running;
                                         int stop_neh;
                                         int rc_running;
                                         int stop_rc;
                                         int rsv_running;
                                         int stop_rsv;

                                         void main();
                                         void shutdown_thread();
                                         int rsv_thread();
                                         int rc_thread();
                                         int neh_thread();


                                         int __shutdown_thread_finished = 0;
                                         int __rsv_thread_finished = 0;
                                         int __rc_thread_finished = 0;
                                         int __neh_thread_finished = 0;


                                         void main() {
__CPROVER_atomic_begin();                  neh_running = 1;                                                       __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  stop_neh = 0;                                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  rc_running = 1;                                                        __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  stop_rc = 0;                                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  rsv_running = 1;                                                       __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  stop_rsv = 0;                                                          __CPROVER_atomic_end();
                                           __CPROVER_ASYNC_1: shutdown_thread();
                                           __CPROVER_ASYNC_1: rc_thread();
                                           __CPROVER_ASYNC_1: neh_thread();
                                           __CPROVER_ASYNC_1: rsv_thread();
                                           __CPROVER_assume(__shutdown_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__rsv_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__rc_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__neh_thread_finished == 1); __assume_dummy=0;
                                           assert(0);
                                         }

                                         void shutdown_thread() {
__CPROVER_atomic_begin();                  stop_rc = 1;                                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  __CPROVER_assume(!rc_running); __assume_dummy=0;                       __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  stop_neh = 1;                                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  __CPROVER_assume(!neh_running); __assume_dummy=0;                      __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  stop_rsv = 1;                                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  __CPROVER_assume(!rsv_running); __assume_dummy=0;                      __CPROVER_atomic_end();
                                           __shutdown_thread_finished = 1;
                                         }

                                         int rsv_thread() {
__CPROVER_atomic_begin();                  __CPROVER_assume(stop_rsv); __assume_dummy=0;                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  rsv_running = 0;                                                       __CPROVER_atomic_end();
                                           __rsv_thread_finished = 1;
                                         }

                                         int rc_thread() {
__CPROVER_atomic_begin();                  assert(neh_running);                                                   __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  __CPROVER_assume(stop_rc); __assume_dummy=0;                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  rc_running = 0;                                                        __CPROVER_atomic_end();
                                           __rc_thread_finished = 1;
                                         }

                                         int neh_thread() {
__CPROVER_atomic_begin();                  assert(rsv_running);                                                   __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  __CPROVER_assume(stop_neh); __assume_dummy=0;                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  neh_running = 0;                                                       __CPROVER_atomic_end();
                                           __neh_thread_finished = 1;
                                         }


// Line: 75