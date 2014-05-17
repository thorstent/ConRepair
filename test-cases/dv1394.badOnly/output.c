                                         unsigned int nondet_int();
                                         int __assume_dummy = 0;


                                         #define MMAP_THREAD  1
                                         #define IOCTL_THREAD 2
                                         #define STATE0       0
                                         #define INITIALISED  1
                                         #define MAPPED       2
                                         #define INCONSISTENT 3

                                         int mtx;
                                         int want_mtx;
                                         int sem;
                                         int want_sem;
                                         int state;
                                         int vm_consistent;

                                         void main();
                                         int mmap_thread();
                                         int rw_thread();
                                         int ioctl_thread();


                                         int __mmap_thread_finished = 0;
                                         int __rw_thread_finished = 0;
                                         int __ioctl_thread_finished = 0;


                                         void main() {
__CPROVER_atomic_begin();                  mtx = 0;                                                               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  want_mtx = 0;                                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  sem = 1;                                                               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  want_sem = 0;                                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  state = 0;                                                             __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  vm_consistent = 1;                                                     __CPROVER_atomic_end();
                                           __CPROVER_ASYNC_1: mmap_thread();
                                           __CPROVER_ASYNC_1: ioctl_thread();
                                           __CPROVER_ASYNC_1: rw_thread();
                                           __CPROVER_assume(__mmap_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__rw_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__ioctl_thread_finished == 1); __assume_dummy=0;
                                           assert(0);
                                         }

                                         int mmap_thread() {
                                           __CPROVER_atomic_begin();
                                             __CPROVER_assume(sem == 1); __assume_dummy=0;                        
                                             sem = 0;                                                             
                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(vm_consistent == 1);                                            __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  vm_consistent = 0;                                                     __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  vm_consistent = 1;                                                     __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  sem = 1;                                                               __CPROVER_atomic_end();
                                           //noReorderBegin();
__CPROVER_atomic_begin();                    assert(((want_sem == 0) | (mtx == 0)) | (sem != 0));                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    want_mtx = 1;                                                        __CPROVER_atomic_end();
                                             __CPROVER_atomic_begin();
                                               __CPROVER_assume(mtx == 0); __assume_dummy=0;                      
                                               mtx = 1;                                                           
                                               want_mtx = 0;                                                      
                                             __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    assert(state != 3);                                                  __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    state = 3;                                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    state = 1;                                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    state = 3;                                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    state = 2;                                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    mtx = 0;                                                             __CPROVER_atomic_end();
                                           //noReorderBegin();
                                           __mmap_thread_finished = 1;
                                         }

                                         int rw_thread() {
                                           __CPROVER_atomic_begin();
                                             __CPROVER_assume(mtx == 0); __assume_dummy=0;                        
                                             mtx = 1;                                                             
                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(state != 3);                                                    __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  mtx = 0;                                                               __CPROVER_atomic_end();
                                           __rw_thread_finished = 1;
                                         }

                                         int ioctl_thread() {
                                           int old_state;
                                           __CPROVER_atomic_begin();
                                             __CPROVER_assume(mtx == 0); __assume_dummy=0;                        
                                             mtx = 1;                                                             
                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(state != 3);                                                    __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  old_state = state;                                                     __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  state = 3;                                                             __CPROVER_atomic_end();
                                           if (nondet_int()) {
                                           } else {
                                             //noReorderBegin();
__CPROVER_atomic_begin();                      assert(((want_mtx == 0) | (sem == 1)) | (mtx == 0));               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                      want_sem = 2;                                                      __CPROVER_atomic_end();
                                               __CPROVER_atomic_begin();
                                                 __CPROVER_assume(sem == 1); __assume_dummy=0;                    
                                                 sem = 0;                                                         
                                                 want_sem = 0;                                                    
                                               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                      assert(vm_consistent);                                             __CPROVER_atomic_end();
__CPROVER_atomic_begin();                      sem = 1;                                                           __CPROVER_atomic_end();
                                             //noReorderBegin();
                                           }
__CPROVER_atomic_begin();                  state = old_state;                                                     __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  mtx = 0;                                                               __CPROVER_atomic_end();
                                           __ioctl_thread_finished = 1;
                                         }


// Line: 112