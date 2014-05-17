                                         unsigned int nondet_int();
                                         int __assume_dummy = 0;


                                         #define CONFIG_THREAD 1
                                         #define START_THREAD  2

                                         int lock1;
                                         int rtnl_lock;
                                         int want_lock;
                                         int want_rtnl_lock;
                                         int restarted;
                                         int notify;

                                         void main();
                                         int reassoc_thread();
                                         int config_thread();
                                         int iwl3945_bg_alive_start_thread();


                                         int __reassoc_thread_finished = 0;
                                         int __config_thread_finished = 0;
                                         int __iwl3945_bg_alive_start_thread_finished = 0;


                                         void main() {
__CPROVER_atomic_begin();                  lock1 = 0;                                                             __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  want_lock = 0;                                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  rtnl_lock = 0;                                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  want_rtnl_lock = 0;                                                    __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  restarted = 0;                                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  notify = 0;                                                            __CPROVER_atomic_end();
                                           __CPROVER_ASYNC_1: config_thread();
                                           __CPROVER_ASYNC_1: iwl3945_bg_alive_start_thread();
                                           __CPROVER_ASYNC_1: reassoc_thread();
                                           __CPROVER_assume(__reassoc_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__config_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__iwl3945_bg_alive_start_thread_finished == 1); __assume_dummy=0;
                                           assert(0);
                                         }

                                         int reassoc_thread() {
__CPROVER_atomic_begin();                  __CPROVER_assume(notify == 1); __assume_dummy=0;                       __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(restarted == 1);                                                __CPROVER_atomic_end();
                                           __reassoc_thread_finished = 1;
                                         }

                                         int config_thread() {
                                           //noReorderBegin();
                                             __CPROVER_atomic_begin();
                                               __CPROVER_assume(rtnl_lock == 0); __assume_dummy=0;                
                                               rtnl_lock = 1;                                                     
                                             __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    want_lock = 1;                                                       __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    assert((lock1 == 0) | (want_rtnl_lock != 2));                        __CPROVER_atomic_end();
                                             __CPROVER_atomic_begin();
                                               __CPROVER_assume(lock1 == 0); __assume_dummy=0;                    
                                               lock1 = 1;                                                         
                                               want_lock = 0;                                                     
                                             __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    lock1 = 0;                                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    rtnl_lock = 0;                                                       __CPROVER_atomic_end();
                                           //noReorderBegin();
                                           __config_thread_finished = 1;
                                         }

                                         int iwl3945_bg_alive_start_thread() {
                                           __CPROVER_atomic_begin();
                                             __CPROVER_assume(lock1 == 0); __assume_dummy=0;                      
                                             lock1 = 1;                                                           
                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  restarted = 1;                                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  lock1 = 0;                                                             __CPROVER_atomic_end();
                                           //noReorderBegin();
__CPROVER_atomic_begin();                    want_rtnl_lock = 2;                                                  __CPROVER_atomic_end();
                                             __CPROVER_atomic_begin();
                                               __CPROVER_assume(rtnl_lock == 0); __assume_dummy=0;                
                                               rtnl_lock = 1;                                                     
                                               want_rtnl_lock = 0;                                                
                                             __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    notify = 1;                                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    rtnl_lock = 0;                                                       __CPROVER_atomic_end();
                                           //noReorderBegin();
                                           __iwl3945_bg_alive_start_thread_finished = 1;
                                         }


// Line: 88