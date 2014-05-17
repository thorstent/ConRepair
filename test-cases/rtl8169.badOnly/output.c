                                         unsigned int nondet_int();
                                         int __assume_dummy = 0;


                                         #define true  1
                                         #define false 0
                                         #define locked   1
                                         #define unlocked 0
                                         #define PCI_THREAD 5
                                         #define SYSFS_THREAD 6
                                         #define budget 100

                                         typedef unsigned char u8;
                                         typedef int bool;
                                         typedef int mutex_t;
                                         typedef int semaphore_t;
                                         int want_sysfs = 0;
                                         int want_dev = 0;
                                         bool registered = 0;
                                         bool netif_running = 0;
                                         bool irq_enabled = 0;
                                         bool napi_enabled = 0;
                                         bool napi_scheduled = 0;
                                         bool reset_task_ready = 0;
                                         bool sysfs_registered = 0;
                                         mutex_t dev_lock = 0;
                                         mutex_t sysfs_lock = 0;
                                         mutex_t rtnl_lock = 0;
                                         semaphore_t irq_sem = 1;
                                         semaphore_t napi_sem = 1;
                                         u8 IntrStatus;
                                         u8 IntrMask;
                                         u8 intr_mask;
                                         bool dev_on = 0;
                                         void (*hw_start)();

                                         void main();
                                         void drv_xmit_timeout();
                                         void napi_disable();
                                         void network_thread();
                                         void napi_thread();
                                         void sysfs_thread();
                                         void napi_complete();
                                         void workqueue_thread();
                                         void remove_sysfs_files();
                                         int drv_irq();
                                         void drv_reset_task();
                                         int close();
                                         int open();
                                         int drv_open();
                                         void free_irq();
                                         void unregister_netdev();
                                         void device_create_bin_file();
                                         void drv_napi_poll();
                                         void drv_hw_start();
                                         void drv_start_xmit();
                                         void drv_disconnect();
                                         void pci_thread();
                                         int handle_interrupt();
                                         void napi_schedule();
                                         void write_IntrStatus(u8 val);
                                         void device_remove_bin_file();
                                         int register_netdev();
                                         void dev_down();
                                         void napi_enable();
                                         void dev_up();
                                         void dev_thread();
                                         int drv_probe();
                                         void drv_close();
                                         void write_IntrMask(u8 val);
                                         void synchronize_irq();
                                         int request_irq();
                                         void create_sysfs_files();
                                         void irq_thread();
                                         unsigned int drv_sysfs_read(int off);
                                         void deadlock_thread();


                                         int __dummy = 0; //dummy to ensure the line shows up in the trace
                                         int __network_thread_finished = 0;
                                         int __napi_thread_finished = 0;
                                         int __sysfs_thread_finished = 0;
                                         int __workqueue_thread_finished = 0;
                                         int __pci_thread_finished = 0;
                                         int __dev_thread_finished = 0;
                                         int __irq_thread_finished = 0;
                                         int __deadlock_thread_finished = 0;
                                         int __assert_fail = 0;


                                         void main() {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();                  registered = 0;                                                        __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  netif_running = 0;                                                     __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  irq_enabled = 0;                                                       __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  napi_enabled = 0;                                                      __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  napi_scheduled = 0;                                                    __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  reset_task_ready = 0;                                                  __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  sysfs_registered = 0;                                                  __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  dev_lock = 0;                                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  sysfs_lock = 0;                                                        __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  rtnl_lock = 0;                                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  want_sysfs = 0;                                                        __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  want_dev = 0;                                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  irq_sem = 0;                                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  napi_sem = 0;                                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  dev_on = 0;                                                            __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  IntrStatus = 0;                                                        __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  IntrMask = 0;                                                          __CPROVER_atomic_end();
                                           __dummy=0; __CPROVER_ASYNC_1: pci_thread();
                                           __dummy=0; __CPROVER_ASYNC_1: network_thread();
                                           __dummy=0; __CPROVER_ASYNC_1: irq_thread();
                                           __dummy=0; __CPROVER_ASYNC_1: napi_thread();
                                           __dummy=0; __CPROVER_ASYNC_1: workqueue_thread();
                                           __dummy=0; __CPROVER_ASYNC_1: sysfs_thread();
                                           __dummy=0; __CPROVER_ASYNC_1: dev_thread();
                                           __dummy=0; __CPROVER_ASYNC_1: deadlock_thread();
                                           __dummy=0; // function end
                                           __CPROVER_assume(__network_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__napi_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__sysfs_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__workqueue_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__pci_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__dev_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__irq_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__deadlock_thread_finished == 1); __assume_dummy=0;
                                           assert(__assert_fail == 0);
                                         }

                                         void drv_xmit_timeout() {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();                  reset_task_ready = 1;                                                  __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                         }

                                         void napi_disable() {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();                  napi_enabled = 0;                                                      __CPROVER_atomic_end();
__CPROVER_atomic_begin();
                                           __CPROVER_assume(napi_sem == 0); __assume_dummy=0;                     
                                           napi_sem = 1;                                                          
                                                                                                                  __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                         }

                                         void network_thread() {
                                           int open1;
                                           __dummy=0; // function begin
                                           while (nondet_int()) {
                                             __dummy=0; open1 = open();
                                             int __test151;
__CPROVER_atomic_begin();                    __test151 = open1 == 0;                                              __CPROVER_atomic_end();
                                             if (__test151) {
                                               __dummy=0; close();
                                               if (nondet_int()) {
                                                 __dummy=0; drv_xmit_timeout();
                                               } else {
                                                 __dummy=0; drv_start_xmit();
                                               }
                                             }
                                           }
                                           __dummy=0; // function end
                                           __network_thread_finished = 1;
                                         }

                                         void napi_thread() {
                                           __dummy=0; // function begin
                                           while (nondet_int()) {
__CPROVER_atomic_begin();
                                             __CPROVER_assume(napi_sem == 0); __assume_dummy=0;                   
                                             napi_sem = 1;                                                        
                                                                                                                  __CPROVER_atomic_end();
                                             int __test173;
__CPROVER_atomic_begin();                    __test173 = napi_scheduled == 0;                                     __CPROVER_atomic_end();
                                             if (__test173) {
                                             } else {
                                               __dummy=0; drv_napi_poll();
                                             }
__CPROVER_atomic_begin();
                                             napi_sem = 0;                                                        
                                                                                                                  __CPROVER_atomic_end();
                                           }
                                           __dummy=0; // function end
                                           __napi_thread_finished = 1;
                                         }

                                         void sysfs_thread() {
                                           int nondet1;
                                           __dummy=0; // function begin
                                           while (nondet_int()) {
                                             //noReorderBegin();
__CPROVER_atomic_begin();                      want_sysfs = 6;                                                    __CPROVER_atomic_end();
                                               __CPROVER_atomic_begin();
                                                 __CPROVER_assume(sysfs_lock == 0); __assume_dummy=0;             
                                                 sysfs_lock = 6;                                                  
                                                 want_sysfs = 0;                                                  
                                               __CPROVER_atomic_end();
                                             //noReorderBegin();
                                             int __test199;
__CPROVER_atomic_begin();                    __test199 = sysfs_registered != 0;                                   __CPROVER_atomic_end();
                                             if (__test199) {
__CPROVER_atomic_begin();                      nondet1 = nondet_int();                                            __CPROVER_atomic_end();
                                               __dummy=0; drv_sysfs_read(nondet1);
                                             }
__CPROVER_atomic_begin();
                                             sysfs_lock = 0;                                                      
                                                                                                                  __CPROVER_atomic_end();
                                             //noReorderBegin();
__CPROVER_atomic_begin();                      want_dev = 6;                                                      __CPROVER_atomic_end();
                                               __CPROVER_atomic_begin();
                                                 __CPROVER_assume(dev_lock == 0); __assume_dummy=0;               
                                                 dev_lock = 6;                                                    
                                                 want_dev = 0;                                                    
                                               __CPROVER_atomic_end();
                                             //noReorderBegin();
__CPROVER_atomic_begin();
                                             dev_lock = 0;                                                        
                                                                                                                  __CPROVER_atomic_end();
                                           }
                                           __dummy=0; // function end
                                           __sysfs_thread_finished = 1;
                                         }

                                         void napi_complete() {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();                  napi_scheduled = 0;                                                    __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                         }

                                         void workqueue_thread() {
                                           __dummy=0; // function begin
                                           while (nondet_int()) {
                                             int __test233;
__CPROVER_atomic_begin();                    __test233 = reset_task_ready == 0;                                   __CPROVER_atomic_end();
                                             if (__test233) {
                                             } else {
                                               __dummy=0; drv_reset_task();
__CPROVER_atomic_begin();                      reset_task_ready = 0;                                              __CPROVER_atomic_end();
                                             }
                                           }
                                           __dummy=0; // function end
                                           __workqueue_thread_finished = 1;
                                         }

                                         void remove_sysfs_files() {
                                           __dummy=0; // function begin
                                           //noReorderBegin();
__CPROVER_atomic_begin();                    want_sysfs = 5;                                                      __CPROVER_atomic_end();
                                             __CPROVER_atomic_begin();
                                               __CPROVER_assume(sysfs_lock == 0); __assume_dummy=0;               
                                               sysfs_lock = 5;                                                    
                                               want_sysfs = 0;                                                    
                                             __CPROVER_atomic_end();
                                           //noReorderBegin();
                                           __dummy=0; device_remove_bin_file();
__CPROVER_atomic_begin();
                                           sysfs_lock = 0;                                                        
                                                                                                                  __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                         }

                                         int drv_irq() {
                                           int handled2;
                                           u8 status1;
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();                  handled2 = 0;                                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  status1 = IntrStatus;                                                  __CPROVER_atomic_end();
                                           while (nondet_int()) {
                                             __dummy=0; write_IntrStatus(status1);
__CPROVER_atomic_begin();                    status1 = IntrStatus;                                                __CPROVER_atomic_end();
                                             int __test271;
__CPROVER_atomic_begin();                    __test271 = intr_mask != 0;                                          __CPROVER_atomic_end();
                                             if (__test271) {
                                               __dummy=0; write_IntrMask(0);
__CPROVER_atomic_begin();                      intr_mask = 0;                                                     __CPROVER_atomic_end();
                                               __dummy=0; napi_schedule();
__CPROVER_atomic_begin();                      handled2 = 1;                                                      __CPROVER_atomic_end();
                                             }
                                           }
__CPROVER_atomic_begin();                  __CPROVER_assume(!status1); __assume_dummy=0;                          __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                           return handled2;
                                         }

                                         void drv_reset_task() {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();
                                           __CPROVER_assume(rtnl_lock == 0); __assume_dummy=0;                    
                                           rtnl_lock = 1;                                                         
                                                                                                                  __CPROVER_atomic_end();
                                           int __test291;
__CPROVER_atomic_begin();                  __test291 = (!netif_running) == 0;                                     __CPROVER_atomic_end();
                                           if (__test291) {
                                             __dummy=0; napi_disable();
                                             __dummy=0; dev_down();
                                             __dummy=0; dev_up();
                                             __dummy=0; napi_enable();
__CPROVER_atomic_begin();
                                             rtnl_lock = 0;                                                       
                                                                                                                  __CPROVER_atomic_end();
                                           }
                                           __dummy=0; // function end
                                         }

                                         int close() {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();
                                           __CPROVER_assume(rtnl_lock == 0); __assume_dummy=0;                    
                                           rtnl_lock = 1;                                                         
                                                                                                                  __CPROVER_atomic_end();
                                           int __test311;
__CPROVER_atomic_begin();                  __test311 = netif_running != 0;                                        __CPROVER_atomic_end();
                                           if (__test311) {
                                             __dummy=0; drv_close();
__CPROVER_atomic_begin();                    netif_running = 0;                                                   __CPROVER_atomic_end();
                                           }
__CPROVER_atomic_begin();
                                           rtnl_lock = 0;                                                         
                                                                                                                  __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                         }

                                         int open() {
                                           int ret1;
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();                  ret1 = 0;                                                              __CPROVER_atomic_end();
                                           int __test327;
__CPROVER_atomic_begin();                  __test327 = registered != 0;                                           __CPROVER_atomic_end();
                                           if (__test327) {
                                             __dummy=0; ret1 = drv_open();
                                             int __test331;
__CPROVER_atomic_begin();                    __test331 = ret1 == 0;                                               __CPROVER_atomic_end();
                                             if (__test331) {
__CPROVER_atomic_begin();                      netif_running = 1;                                                 __CPROVER_atomic_end();
                                             }
                                           }
__CPROVER_atomic_begin();
                                           __CPROVER_assume(rtnl_lock == 0); __assume_dummy=0;                    
                                           rtnl_lock = 1;                                                         
                                                                                                                  __CPROVER_atomic_end();
__CPROVER_atomic_begin();
                                           rtnl_lock = 0;                                                         
                                                                                                                  __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                           return ret1;
                                         }

                                         int drv_open() {
                                           int rc2;
                                           int ret5;
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();

                                           if (hw_start != 0)
                                             __dummy=0; //assertion passed
                                           else
                                             __assert_fail=1;
                                                                                                                  
                                                                                                                  __CPROVER_atomic_end();
                                           __dummy=0; rc2 = request_irq();
__CPROVER_atomic_begin();                  IntrMask = 0;                                                          __CPROVER_atomic_end();
                                           int __test362;
__CPROVER_atomic_begin();                  __test362 = rc2 < 0;                                                   __CPROVER_atomic_end();
                                           if (__test362) {
__CPROVER_atomic_begin();                    ret5 = rc2;                                                          __CPROVER_atomic_end();
                                           } else {
                                             __dummy=0; napi_enable();
                                             __dummy=0; dev_up();
__CPROVER_atomic_begin();                    ret5 = 0;                                                            __CPROVER_atomic_end();
                                           }
                                           __dummy=0; // function end
                                           return ret5;
                                         }

                                         void free_irq() {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();                  irq_enabled = 0;                                                       __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                         }

                                         void unregister_netdev() {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();                  registered = 0;                                                        __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  __CPROVER_assume(!(netif_running != 0)); __assume_dummy=0;             __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                         }

                                         void device_create_bin_file() {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();                  sysfs_registered = 1;                                                  __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                         }

                                         void drv_napi_poll() {
                                           int work_done;
                                           __dummy=0; // function begin
                                           __dummy=0; work_done = handle_interrupt();
                                           int __test398;
__CPROVER_atomic_begin();                  __test398 = work_done < 100;                                           __CPROVER_atomic_end();
                                           if (__test398) {
                                             __dummy=0; napi_complete();
__CPROVER_atomic_begin();                    IntrMask = 255;                                                      __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    intr_mask = 255;                                                     __CPROVER_atomic_end();
                                           }
                                           __dummy=0; // function end
                                         }

                                         void drv_hw_start() {
                                           __dummy=0; // function begin
                                           __dummy=0; // function end
                                         }

                                         void drv_start_xmit() {
                                           __dummy=0; // function begin
                                           __dummy=0; // function end
                                         }

                                         void drv_disconnect() {
                                           __dummy=0; // function begin
                                           __dummy=0; unregister_netdev();
                                           __dummy=0; remove_sysfs_files();
                                           __dummy=0; // function end
                                         }

                                         void pci_thread() {
                                           int probe1;
                                           __dummy=0; // function begin
                                           __dummy=0; probe1 = drv_probe();
                                           int __test429;
__CPROVER_atomic_begin();                  __test429 = probe1 == 0;                                               __CPROVER_atomic_end();
                                           if (__test429) {
                                             __dummy=0; drv_disconnect();
                                             //noReorderBegin();
__CPROVER_atomic_begin();                      want_dev = 5;                                                      __CPROVER_atomic_end();
                                               __CPROVER_atomic_begin();
                                                 __CPROVER_assume(dev_lock == 0); __assume_dummy=0;               
                                                 dev_lock = 5;                                                    
                                                 want_dev = 0;                                                    
                                               __CPROVER_atomic_end();
                                             //noReorderBegin();
__CPROVER_atomic_begin();
                                             dev_lock = 0;                                                        
                                                                                                                  __CPROVER_atomic_end();
                                           }
                                           __dummy=0; // function end
                                           __pci_thread_finished = 1;
                                         }

                                         int handle_interrupt() {
                                           int nondet3;
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();                  nondet3 = nondet_int();                                                __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  IntrStatus = 0;                                                        __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                           return nondet3;
                                         }

                                         void napi_schedule() {
                                           __dummy=0; // function begin
                                           __CPROVER_atomic_begin();
                                             int __test461;
                                             __test461 = napi_enabled != 0;                                       
                                             if (__test461) {
                                               napi_scheduled = 1;                                                
                                             }
                                           __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                         }

                                         void write_IntrStatus(u8 val) {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();
                                           if (dev_on != 0)
                                             __dummy=0; //assertion passed
                                           else
                                             __assert_fail=1;
                                                                                                                  __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  IntrStatus = IntrStatus & (~val);                                      __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                         }

                                         void device_remove_bin_file() {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();                  sysfs_registered = 0;                                                  __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                         }

                                         int register_netdev() {
                                           int ret2;
                                           __dummy=0; // function begin
                                           if (nondet_int()) {
__CPROVER_atomic_begin();                    ret2 = -1;                                                           __CPROVER_atomic_end();
                                           } else {
__CPROVER_atomic_begin();                    registered = 1;                                                      __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    ret2 = 0;                                                            __CPROVER_atomic_end();
                                           }
                                           __dummy=0; // function end
                                           return ret2;
                                         }

                                         void dev_down() {
                                           __dummy=0; // function begin
                                           __dummy=0; write_IntrMask(0);
                                           __dummy=0; synchronize_irq();
__CPROVER_atomic_begin();                  dev_on = 0;                                                            __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                         }

                                         void napi_enable() {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();                  napi_enabled = 1;                                                      __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                         }

                                         void dev_up() {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();                  dev_on = 1;                                                            __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  intr_mask = 1;                                                         __CPROVER_atomic_end();
                                           __dummy=0; write_IntrMask(1);
                                           __dummy=0; // function end
                                         }

                                         void dev_thread() {
                                           __dummy=0; // function begin
                                           while (nondet_int()) {
__CPROVER_atomic_begin();                    IntrStatus = 1;                                                      __CPROVER_atomic_end();
                                           }
                                           __dummy=0; // function end
                                           __dev_thread_finished = 1;
                                         }

                                         int drv_probe() {
                                           int rc1;
                                           int ret4;
                                           __dummy=0; // function begin
                                           __dummy=0; create_sysfs_files();
__CPROVER_atomic_begin();                  hw_start = drv_hw_start;                                               __CPROVER_atomic_end();
                                           __dummy=0; rc1 = register_netdev();
                                           int __test539;
__CPROVER_atomic_begin();                  __test539 = rc1 < 0;                                                   __CPROVER_atomic_end();
                                           if (__test539) {
__CPROVER_atomic_begin();                    ret4 = rc1;                                                          __CPROVER_atomic_end();
                                           } else {
__CPROVER_atomic_begin();                    ret4 = 0;                                                            __CPROVER_atomic_end();
                                           }
                                           __dummy=0; // function end
                                           return ret4;
                                         }

                                         void drv_close() {
                                           __dummy=0; // function begin
                                           __dummy=0; free_irq();
                                           __dummy=0; dev_down();
                                           __dummy=0; napi_disable();
                                           __dummy=0; // function end
                                         }

                                         void write_IntrMask(u8 val) {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();
                                           if (dev_on != 0)
                                             __dummy=0; //assertion passed
                                           else
                                             __assert_fail=1;
                                                                                                                  __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  IntrMask = val;                                                        __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                         }

                                         void synchronize_irq() {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();
                                           __CPROVER_assume(irq_sem == 0); __assume_dummy=0;                      
                                           irq_sem = 1;                                                           
                                                                                                                  __CPROVER_atomic_end();
__CPROVER_atomic_begin();
                                           irq_sem = 0;                                                           
                                                                                                                  __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                         }

                                         int request_irq() {
                                           int ret3;
                                           __dummy=0; // function begin
                                           if (nondet_int()) {
__CPROVER_atomic_begin();                    ret3 = -1;                                                           __CPROVER_atomic_end();
                                           } else {
__CPROVER_atomic_begin();                    irq_enabled = 1;                                                     __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    ret3 = 0;                                                            __CPROVER_atomic_end();
                                           }
                                           __dummy=0; // function end
                                           return ret3;
                                         }

                                         void create_sysfs_files() {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();
                                           __CPROVER_assume(sysfs_lock == 0); __assume_dummy=0;                   
                                           sysfs_lock = 1;                                                        
                                                                                                                  __CPROVER_atomic_end();
                                           __dummy=0; device_create_bin_file();
__CPROVER_atomic_begin();
                                           sysfs_lock = 0;                                                        
                                                                                                                  __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                         }

                                         void irq_thread() {
                                           __dummy=0; // function begin
                                           while (nondet_int()) {
__CPROVER_atomic_begin();
                                             __CPROVER_assume(irq_sem == 0); __assume_dummy=0;                    
                                             irq_sem = 1;                                                         
                                                                                                                  __CPROVER_atomic_end();
__CPROVER_atomic_begin();
                                             irq_sem = 0;                                                         
                                                                                                                  __CPROVER_atomic_end();
                                             int __test618;
__CPROVER_atomic_begin();                    __test618 = (((irq_enabled != 0) & (IntrStatus != 0)) & (IntrMask != 0)) == 0;__CPROVER_atomic_end();
                                             if (__test618) {
                                             } else {
__CPROVER_atomic_begin();
                                               if (intr_mask != 0)
                                                 __dummy=0; //assertion passed
                                               else
                                                 __assert_fail=1;
                                                                                                                  __CPROVER_atomic_end();
                                               __dummy=0; drv_irq();
                                             }
                                           }
                                           __dummy=0; // function end
                                           __irq_thread_finished = 1;
                                         }

                                         unsigned int drv_sysfs_read(int off) {
                                           int nondet2;
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();                  nondet2 = nondet_int();                                                __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                           return nondet2;
                                         }

                                         void deadlock_thread() {
                                           __dummy=0; // function begin
__CPROVER_atomic_begin();
                                           if (!((((dev_lock == want_sysfs) & (sysfs_lock == want_dev)) & (dev_lock != 0)) & (sysfs_lock != 0)))
                                             __dummy=0; //assertion passed
                                           else
                                             __assert_fail=1;
                                                                                                                  __CPROVER_atomic_end();
                                           __dummy=0; // function end
                                           __deadlock_thread_finished = 1;
                                         }


// Line: 656