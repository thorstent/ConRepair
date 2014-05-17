                                         unsigned int nondet_int();
                                         int __assume_dummy = 0;


                                         #define CLIENT1 1
                                         #define CLIENT2 2
                                         #define WORKER  3

                                         int lock1;
                                         int bsy;
                                         int pending;
                                         int request;

                                         void main();
                                         int acm_cdc_notify(int thread_id);
                                         int worker_thread();
                                         int client_thread1();
                                         int client_thread2();




                                         void main() {
__CPROVER_atomic_begin();                  lock1 = 0;                                                             __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  bsy = 0;                                                               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  pending = 0;                                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  request = 0;                                                           __CPROVER_atomic_end();
                                           __CPROVER_ASYNC_1: client_thread1();
                                           __CPROVER_ASYNC_1: client_thread2();
                                           __CPROVER_ASYNC_1: worker_thread();
                                            }

                                         int acm_cdc_notify(int thread_id) {
                                           int result;
__CPROVER_atomic_begin();                  result = 0;                                                            __CPROVER_atomic_end();
                                           __CPROVER_atomic_begin();
                                             __CPROVER_assume(lock1 == 0); __assume_dummy=0;                      
                                             lock1 = thread_id;                                                   
                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  pending = 0;                                                           __CPROVER_atomic_end();
                                           if (bsy == 0) {
__CPROVER_atomic_begin();                    bsy = 1;                                                             __CPROVER_atomic_end();
                                             __CPROVER_atomic_begin();
                                               lock1 = 0;                                                         
                                               if (nondet_int()) {
                                                 result = -1;                                                     
                                               } else {
                                                 request = thread_id;                                             
                                               }
                                               if (result == (-1)) {
                                                 bsy = 0;                                                         
                                               }
                                             __CPROVER_atomic_end();
                                           } else {
__CPROVER_atomic_begin();                    pending = 1;                                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    assert(bsy);                                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    lock1 = 0;                                                           __CPROVER_atomic_end();
                                           }
                                         }

                                         int worker_thread() {
                                           while (nondet_int()) {
__CPROVER_atomic_begin();                    __CPROVER_assume(request); __assume_dummy=0;                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    assert(lock1 != request);                                            __CPROVER_atomic_end();
                                             __CPROVER_atomic_begin();
                                               __CPROVER_assume(lock1 == 0); __assume_dummy=0;                    
                                               lock1 = 3;                                                         
                                             __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    bsy = 0;                                                             __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    request = 0;                                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    lock1 = 0;                                                           __CPROVER_atomic_end();
                                             if (pending == 0) {
                                             } else {
                                               acm_cdc_notify(3);
                                             }
                                           }
                                         }

                                         int client_thread1() {
                                           acm_cdc_notify(1);
                                         }

                                         int client_thread2() {
                                           acm_cdc_notify(2);
                                         }


// Line: 88