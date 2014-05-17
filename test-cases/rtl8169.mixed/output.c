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


                                         int __network_thread_finished = 0;
                                         int __napi_thread_finished = 0;
                                         int __sysfs_thread_finished = 0;
                                         int __workqueue_thread_finished = 0;
                                         int __pci_thread_finished = 0;
                                         int __dev_thread_finished = 0;
                                         int __irq_thread_finished = 0;
                                         int __deadlock_thread_finished = 0;


                                         void main() {
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
                                           __CPROVER_ASYNC_1: pci_thread();
                                           __CPROVER_ASYNC_1: network_thread();
                                           __CPROVER_ASYNC_1: irq_thread();
                                           __CPROVER_ASYNC_1: napi_thread();
                                           __CPROVER_ASYNC_1: workqueue_thread();
                                           __CPROVER_ASYNC_1: sysfs_thread();
                                           __CPROVER_ASYNC_1: dev_thread();
                                           __CPROVER_ASYNC_1: deadlock_thread();
                                           __CPROVER_assume(__network_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__napi_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__sysfs_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__workqueue_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__pci_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__dev_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__irq_thread_finished == 1); __assume_dummy=0;
                                           __CPROVER_assume(__deadlock_thread_finished == 1); __assume_dummy=0;
                                           assert(0);
                                         }

                                         void drv_xmit_timeout() {
__CPROVER_atomic_begin();                  reset_task_ready = 1;                                                  __CPROVER_atomic_end();
                                         }

                                         void napi_disable() {
__CPROVER_atomic_begin();                  napi_enabled = 0;                                                      __CPROVER_atomic_end();
__CPROVER_atomic_begin();
                                           __CPROVER_assume(napi_sem == 0); __assume_dummy=0;                     
                                           napi_sem = 1;                                                          
                                                                                                                  __CPROVER_atomic_end();
                                         }

                                         void network_thread() {
                                           int open1;
                                           while (nondet_int()) {
                                             open1 = open();
                                             if (open1 == 0) {
                                               if (nondet_int()) {
                                                 drv_xmit_timeout();
                                               } else {
                                                 drv_start_xmit();
                                               }
                                               close();
                                             }
                                           }
                                           __network_thread_finished = 1;
                                         }

                                         void napi_thread() {
                                           while (nondet_int()) {
__CPROVER_atomic_begin();
                                             __CPROVER_assume(napi_sem == 0); __assume_dummy=0;                   
                                             napi_sem = 1;                                                        
                                                                                                                  __CPROVER_atomic_end();
                                             if (napi_scheduled == 0) {
                                             } else {
                                               drv_napi_poll();
                                             }
__CPROVER_atomic_begin();
                                             napi_sem = 0;                                                        
                                                                                                                  __CPROVER_atomic_end();
                                           }
                                           __napi_thread_finished = 1;
                                         }

                                         void sysfs_thread() {
                                           int nondet1;
                                           while (nondet_int()) {
                                             //noReorderBegin();
__CPROVER_atomic_begin();                      want_dev = 6;                                                      __CPROVER_atomic_end();
                                               __CPROVER_atomic_begin();
                                                 __CPROVER_assume(dev_lock == 0); __assume_dummy=0;               
                                                 dev_lock = 6;                                                    
                                                 want_dev = 0;                                                    
                                               __CPROVER_atomic_end();
                                             //noReorderBegin();
                                             //noReorderBegin();
__CPROVER_atomic_begin();                      want_sysfs = 6;                                                    __CPROVER_atomic_end();
                                               __CPROVER_atomic_begin();
                                                 __CPROVER_assume(sysfs_lock == 0); __assume_dummy=0;             
                                                 sysfs_lock = 6;                                                  
                                                 want_sysfs = 0;                                                  
                                               __CPROVER_atomic_end();
                                             //noReorderBegin();
                                             if (sysfs_registered != 0) {
__CPROVER_atomic_begin();                      nondet1 = nondet_int();                                            __CPROVER_atomic_end();
                                               drv_sysfs_read(nondet1);
                                             }
__CPROVER_atomic_begin();
                                             dev_lock = 0;                                                        
                                                                                                                  __CPROVER_atomic_end();
__CPROVER_atomic_begin();
                                             sysfs_lock = 0;                                                      
                                                                                                                  __CPROVER_atomic_end();
                                           }
                                           __sysfs_thread_finished = 1;
                                         }

                                         void napi_complete() {
__CPROVER_atomic_begin();                  napi_scheduled = 0;                                                    __CPROVER_atomic_end();
                                         }

                                         void workqueue_thread() {
                                           while (nondet_int()) {
                                             if (reset_task_ready == 0) {
                                             } else {
                                               drv_reset_task();
__CPROVER_atomic_begin();                      reset_task_ready = 0;                                              __CPROVER_atomic_end();
                                             }
                                           }
                                           __workqueue_thread_finished = 1;
                                         }

                                         void remove_sysfs_files() {
                                           //noReorderBegin();
__CPROVER_atomic_begin();                    want_sysfs = 5;                                                      __CPROVER_atomic_end();
                                             __CPROVER_atomic_begin();
                                               __CPROVER_assume(sysfs_lock == 0); __assume_dummy=0;               
                                               sysfs_lock = 5;                                                    
                                               want_sysfs = 0;                                                    
                                             __CPROVER_atomic_end();
                                           //noReorderBegin();
                                           device_remove_bin_file();
__CPROVER_atomic_begin();
                                           sysfs_lock = 0;                                                        
                                                                                                                  __CPROVER_atomic_end();
                                         }

                                         int drv_irq() {
                                           int handled2;
                                           u8 status1;
__CPROVER_atomic_begin();                  handled2 = 0;                                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  status1 = IntrStatus;                                                  __CPROVER_atomic_end();
                                           while (nondet_int()) {
                                             if (intr_mask != 0) {
                                               write_IntrMask(0);
__CPROVER_atomic_begin();                      intr_mask = 0;                                                     __CPROVER_atomic_end();
                                               napi_schedule();
__CPROVER_atomic_begin();                      handled2 = 1;                                                      __CPROVER_atomic_end();
                                             }
                                             write_IntrStatus(status1);
__CPROVER_atomic_begin();                    status1 = IntrStatus;                                                __CPROVER_atomic_end();
                                           }
__CPROVER_atomic_begin();                  __CPROVER_assume(!status1); __assume_dummy=0;                          __CPROVER_atomic_end();
                                           return handled2;
                                         }

                                         void drv_reset_task() {
__CPROVER_atomic_begin();
                                           __CPROVER_assume(rtnl_lock == 0); __assume_dummy=0;                    
                                           rtnl_lock = 1;                                                         
                                                                                                                  __CPROVER_atomic_end();
                                           if ((!netif_running) == 0) {
                                             napi_disable();
                                             dev_down();
                                             dev_up();
                                             napi_enable();
__CPROVER_atomic_begin();
                                             rtnl_lock = 0;                                                       
                                                                                                                  __CPROVER_atomic_end();
                                           }
                                         }

                                         int close() {
__CPROVER_atomic_begin();
                                           __CPROVER_assume(rtnl_lock == 0); __assume_dummy=0;                    
                                           rtnl_lock = 1;                                                         
                                                                                                                  __CPROVER_atomic_end();
                                           if (netif_running != 0) {
__CPROVER_atomic_begin();                    netif_running = 0;                                                   __CPROVER_atomic_end();
                                             drv_close();
                                           }
__CPROVER_atomic_begin();
                                           rtnl_lock = 0;                                                         
                                                                                                                  __CPROVER_atomic_end();
                                         }

                                         int open() {
                                           int ret1;
__CPROVER_atomic_begin();                  ret1 = 0;                                                              __CPROVER_atomic_end();
__CPROVER_atomic_begin();
                                           __CPROVER_assume(rtnl_lock == 0); __assume_dummy=0;                    
                                           rtnl_lock = 1;                                                         
                                                                                                                  __CPROVER_atomic_end();
                                           if (registered != 0) {
                                             ret1 = drv_open();
                                             if (ret1 == 0) {
__CPROVER_atomic_begin();                      netif_running = 1;                                                 __CPROVER_atomic_end();
                                             }
                                           }
__CPROVER_atomic_begin();
                                           rtnl_lock = 0;                                                         
                                                                                                                  __CPROVER_atomic_end();
                                           return ret1;
                                         }

                                         int drv_open() {
                                           int rc2;
                                           int ret5;
__CPROVER_atomic_begin();
                                           assert(hw_start != 0);                                                 
                                                                                                                  __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  IntrMask = 0;                                                          __CPROVER_atomic_end();
                                           rc2 = request_irq();
                                           if (rc2 < 0) {
__CPROVER_atomic_begin();                    ret5 = rc2;                                                          __CPROVER_atomic_end();
                                           } else {
                                             napi_enable();
                                             dev_up();
__CPROVER_atomic_begin();                    ret5 = 0;                                                            __CPROVER_atomic_end();
                                           }
                                           return ret5;
                                         }

                                         void free_irq() {
__CPROVER_atomic_begin();                  irq_enabled = 0;                                                       __CPROVER_atomic_end();
                                         }

                                         void unregister_netdev() {
__CPROVER_atomic_begin();                  registered = 0;                                                        __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  __CPROVER_assume(!(netif_running != 0)); __assume_dummy=0;             __CPROVER_atomic_end();
                                         }

                                         void device_create_bin_file() {
__CPROVER_atomic_begin();                  sysfs_registered = 1;                                                  __CPROVER_atomic_end();
                                         }

                                         void drv_napi_poll() {
                                           int work_done;
                                           work_done = handle_interrupt();
                                           if (work_done < 100) {
                                             napi_complete();
                                             __CPROVER_atomic_begin();
                                               intr_mask = 255;                                                   
                                               IntrMask = 255;                                                    
                                             __CPROVER_atomic_end();
                                           }
                                         }

                                         void drv_hw_start() {
                                         }

                                         void drv_start_xmit() {
                                         }

                                         void drv_disconnect() {
                                           unregister_netdev();
                                           remove_sysfs_files();
                                         }

                                         void pci_thread() {
                                           int probe1;
                                           probe1 = drv_probe();
                                           if (probe1 == 0) {
                                             //noReorderBegin();
__CPROVER_atomic_begin();                      want_dev = 5;                                                      __CPROVER_atomic_end();
                                               __CPROVER_atomic_begin();
                                                 __CPROVER_assume(dev_lock == 0); __assume_dummy=0;               
                                                 dev_lock = 5;                                                    
                                                 want_dev = 0;                                                    
                                               __CPROVER_atomic_end();
                                             //noReorderBegin();
                                             drv_disconnect();
__CPROVER_atomic_begin();
                                             dev_lock = 0;                                                        
                                                                                                                  __CPROVER_atomic_end();
                                           }
                                           __pci_thread_finished = 1;
                                         }

                                         int handle_interrupt() {
                                           int nondet3;
__CPROVER_atomic_begin();                  nondet3 = nondet_int();                                                __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  IntrStatus = 0;                                                        __CPROVER_atomic_end();
                                           return nondet3;
                                         }

                                         void napi_schedule() {
                                           __CPROVER_atomic_begin();
                                             if (napi_enabled != 0) {
                                               napi_scheduled = 1;                                                
                                             }
                                           __CPROVER_atomic_end();
                                         }

                                         void write_IntrStatus(u8 val) {
__CPROVER_atomic_begin();                  assert(dev_on != 0);                                                   __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  IntrStatus = IntrStatus & (~val);                                      __CPROVER_atomic_end();
                                         }

                                         void device_remove_bin_file() {
__CPROVER_atomic_begin();                  sysfs_registered = 0;                                                  __CPROVER_atomic_end();
                                         }

                                         int register_netdev() {
                                           int ret2;
                                           if (nondet_int()) {
__CPROVER_atomic_begin();                    ret2 = -1;                                                           __CPROVER_atomic_end();
                                           } else {
__CPROVER_atomic_begin();                    registered = 1;                                                      __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    ret2 = 0;                                                            __CPROVER_atomic_end();
                                           }
                                           return ret2;
                                         }

                                         void dev_down() {
                                           write_IntrMask(0);
                                           synchronize_irq();
__CPROVER_atomic_begin();                  dev_on = 0;                                                            __CPROVER_atomic_end();
                                         }

                                         void napi_enable() {
__CPROVER_atomic_begin();                  napi_enabled = 1;                                                      __CPROVER_atomic_end();
                                         }

                                         void dev_up() {
__CPROVER_atomic_begin();                  dev_on = 1;                                                            __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  intr_mask = 1;                                                         __CPROVER_atomic_end();
                                           write_IntrMask(1);
                                         }

                                         void dev_thread() {
                                           while (nondet_int()) {
__CPROVER_atomic_begin();                    IntrStatus = 1;                                                      __CPROVER_atomic_end();
                                           }
                                           __dev_thread_finished = 1;
                                         }

                                         int drv_probe() {
                                           int rc1;
                                           int ret4;
                                           create_sysfs_files();
__CPROVER_atomic_begin();                  hw_start = drv_hw_start;                                               __CPROVER_atomic_end();
                                           rc1 = register_netdev();
                                           if (rc1 < 0) {
__CPROVER_atomic_begin();                    ret4 = rc1;                                                          __CPROVER_atomic_end();
                                           } else {
__CPROVER_atomic_begin();                    ret4 = 0;                                                            __CPROVER_atomic_end();
                                           }
                                           return ret4;
                                         }

                                         void drv_close() {
                                           free_irq();
                                           dev_down();
                                           napi_disable();
                                         }

                                         void write_IntrMask(u8 val) {
__CPROVER_atomic_begin();                  assert(dev_on != 0);                                                   __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  IntrMask = val;                                                        __CPROVER_atomic_end();
                                         }

                                         void synchronize_irq() {
__CPROVER_atomic_begin();
                                           __CPROVER_assume(irq_sem == 0); __assume_dummy=0;                      
                                           irq_sem = 1;                                                           
                                                                                                                  __CPROVER_atomic_end();
__CPROVER_atomic_begin();
                                           irq_sem = 0;                                                           
                                                                                                                  __CPROVER_atomic_end();
                                         }

                                         int request_irq() {
                                           int ret3;
                                           if (nondet_int()) {
__CPROVER_atomic_begin();                    ret3 = -1;                                                           __CPROVER_atomic_end();
                                           } else {
__CPROVER_atomic_begin();                    irq_enabled = 1;                                                     __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    ret3 = 0;                                                            __CPROVER_atomic_end();
                                           }
                                           return ret3;
                                         }

                                         void create_sysfs_files() {
__CPROVER_atomic_begin();
                                           __CPROVER_assume(sysfs_lock == 0); __assume_dummy=0;                   
                                           sysfs_lock = 1;                                                        
                                                                                                                  __CPROVER_atomic_end();
                                           device_create_bin_file();
__CPROVER_atomic_begin();
                                           sysfs_lock = 0;                                                        
                                                                                                                  __CPROVER_atomic_end();
                                         }

                                         void irq_thread() {
                                           while (nondet_int()) {
__CPROVER_atomic_begin();
                                             __CPROVER_assume(irq_sem == 0); __assume_dummy=0;                    
                                             irq_sem = 1;                                                         
                                                                                                                  __CPROVER_atomic_end();
                                             if ((((irq_enabled != 0) & (IntrStatus != 0)) & (IntrMask != 0)) == 0) {
                                             } else {
__CPROVER_atomic_begin();                      assert(intr_mask != 0);                                            __CPROVER_atomic_end();
                                               drv_irq();
                                             }
__CPROVER_atomic_begin();
                                             irq_sem = 0;                                                         
                                                                                                                  __CPROVER_atomic_end();
                                           }
                                           __irq_thread_finished = 1;
                                         }

                                         unsigned int drv_sysfs_read(int off) {
                                           int nondet2;
__CPROVER_atomic_begin();                  nondet2 = nondet_int();                                                __CPROVER_atomic_end();
                                           return nondet2;
                                         }

                                         void deadlock_thread() {
__CPROVER_atomic_begin();                  assert(!((((dev_lock == want_sysfs) & (sysfs_lock == want_dev)) & (dev_lock != 0)) & (sysfs_lock != 0)));__CPROVER_atomic_end();
                                           __deadlock_thread_finished = 1;
                                         }


// Line: 521