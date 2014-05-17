                                         unsigned int nondet_int();
                                         int __assume_dummy = 0;


                                         #define USB_BUS_THREAD 2
                                         #define URB_IDLE      0
                                         #define URB_SUBMITTED 1

                                         int fw_tty_registered;
                                         int fw_tty_initialized;
                                         int fw_tty_lock;
                                         int fw_driver_list_consistent;
                                         int fw_idr_consistent;
                                         int fw_table_lock;
                                         int fw_serial_bus_lock;
                                         int drv_usb_registered;
                                         int drv_usb_initialized;
                                         int drv_registered_with_usb_fw;
                                         int drv_registered_with_serial_fw;
                                         int drv_id_table;
                                         int drv_module_ref_cnt;
                                         int drv_device_id_registered;
                                         int dev_usb_serial_initialized;
                                         int dev_autopm;
                                         int dev_disconnected;
                                         int dev_disc_lock;
                                         int port_dev_registered;
                                         int port_initialized;
                                         int port_idr_registered;
                                         int port_tty_installed;
                                         int port_work;
                                         int port_work_initialized;
                                         int port_work_stop;
                                         int port_tty_registered;
                                         int port_consistent;
                                         int port_lock;
                                         int port_write_in_progress;
                                         int write_urb_state;

                                         void main();
                                         void usb_bus_thread();
                                         void tty_thread();
                                         void usb_cb_thread();
                                         void lock_tty();
                                         void usb_serial_put();
                                         void unlock_port();
                                         void serial_write();
                                         void serial_bus_thread();
                                         void cancel_work_sync();
                                         void lock_disc();
                                         void fw_module_thread();
                                         void belkin_release();
                                         void belkin_init();
                                         void belkin_exit();
                                         void belkin_port_probe();
                                         void unlock_table();
                                         void unlock_serial_bus();
                                         void belkin_disconnect();
                                         void usb_serial_disconnect();
                                         void usb_serial_device_probe();
                                         void lock_table();
                                         void lock_serial_bus();
                                         void lock_port();
                                         void serial_install();
                                         void belkin_port_remove();
                                         void usb_serial_probe();
                                         void try_module_get();
                                         void usb_serial_device_remove();
                                         void serial_hangup();
                                         void usb_serial_init();
                                         void belkin_attach();
                                         void unlock_disc();
                                         void usb_serial_port_poison_urbs();
                                         void unlock_tty();
                                         void port_work_thread();
                                         void usb_serial_exit();
                                         void serial_write_callback();
                                         void belkin_probe();
                                         void attribute_thread();




                                         void main() {
__CPROVER_atomic_begin();                  fw_tty_registered = 0;                                                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  fw_tty_initialized = 0;                                                __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  fw_tty_lock = 0;                                                       __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  fw_driver_list_consistent = 1;                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  fw_idr_consistent = 1;                                                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  fw_table_lock = 0;                                                     __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  fw_serial_bus_lock = 0;                                                __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  drv_usb_registered = 0;                                                __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  drv_usb_initialized = 0;                                               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  drv_registered_with_usb_fw = 0;                                        __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  drv_registered_with_serial_fw = 0;                                     __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  drv_id_table = 0;                                                      __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  drv_module_ref_cnt = 0;                                                __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  drv_device_id_registered = 0;                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  dev_usb_serial_initialized = -1;                                       __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  dev_autopm = 0;                                                        __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  dev_disconnected = 0;                                                  __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  dev_disc_lock = 0;                                                     __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_dev_registered = 0;                                               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_initialized = 0;                                                  __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_tty_registered = 0;                                               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_tty_installed = 0;                                                __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_idr_registered = 0;                                               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_work = 0;                                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_work_initialized = 0;                                             __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_work_stop = 0;                                                    __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_consistent = 1;                                                   __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_lock = 0;                                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_write_in_progress = 0;                                            __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  write_urb_state = 0;                                                   __CPROVER_atomic_end();
                                           __CPROVER_ASYNC_1: fw_module_thread();
                                           __CPROVER_ASYNC_1: usb_bus_thread();
                                           __CPROVER_ASYNC_1: serial_bus_thread();
                                           __CPROVER_ASYNC_1: tty_thread();
                                           __CPROVER_ASYNC_1: attribute_thread();
                                           __CPROVER_ASYNC_1: usb_cb_thread();
                                           __CPROVER_ASYNC_1: port_work_thread();
                                            }

                                         void usb_bus_thread() {
__CPROVER_atomic_begin();                  __CPROVER_assume(drv_usb_registered | drv_device_id_registered); __assume_dummy=0;__CPROVER_atomic_end();
                                           usb_serial_probe();
                                           if (port_dev_registered == 0) {
                                           } else {
                                             usb_serial_disconnect();
                                           }
                                         }

                                         void tty_thread() {
__CPROVER_atomic_begin();                  __CPROVER_assume(drv_registered_with_serial_fw); __assume_dummy=0;     __CPROVER_atomic_end();
                                           serial_install();
                                           lock_tty();
                                           if (port_tty_installed == 0) {
                                           } else {
__CPROVER_atomic_begin();                    port_write_in_progress = 1;                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    assert(port_initialized);                                            __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    assert(dev_usb_serial_initialized);                                  __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    assert(port_tty_installed);                                          __CPROVER_atomic_end();
                                             lock_port();
__CPROVER_atomic_begin();                    assert(port_consistent);                                             __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    port_consistent = 0;                                                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    port_consistent = 1;                                                 __CPROVER_atomic_end();
                                             unlock_port();
                                             if (write_urb_state == 0) {
__CPROVER_atomic_begin();                      write_urb_state = 1;                                               __CPROVER_atomic_end();
                                             }
                                           }
                                           unlock_tty();
                                         }

                                         void usb_cb_thread() {
__CPROVER_atomic_begin();                  __CPROVER_assume((write_urb_state == 1) | (drv_usb_registered == 0)); __assume_dummy=0;__CPROVER_atomic_end();
                                           if (write_urb_state == 1) {
__CPROVER_atomic_begin();                    assert(drv_usb_initialized);                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    write_urb_state = 0;                                                 __CPROVER_atomic_end();
                                             serial_write_callback();
                                           }
                                         }

                                         void lock_tty() {
                                           __CPROVER_atomic_begin();
                                             __CPROVER_assume(fw_tty_lock == 0); __assume_dummy=0;                
                                             fw_tty_lock = 1;                                                     
                                           __CPROVER_atomic_end();
                                         }

                                         void usb_serial_put() {
                                           int old;
__CPROVER_atomic_begin();                  assert(dev_usb_serial_initialized > 0);                                __CPROVER_atomic_end();
                                           __CPROVER_atomic_begin();
                                             old = dev_usb_serial_initialized;                                    
                                             dev_usb_serial_initialized = dev_usb_serial_initialized - 1;         
                                           __CPROVER_atomic_end();
                                           if (old == 1) {
                                             if (port_idr_registered == 0) {
                                             } else {
                                               lock_table();
__CPROVER_atomic_begin();                      assert(fw_idr_consistent);                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                      fw_idr_consistent = 0;                                             __CPROVER_atomic_end();
__CPROVER_atomic_begin();                      port_idr_registered = 0;                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                      fw_idr_consistent = 1;                                             __CPROVER_atomic_end();
                                               unlock_table();
                                             }
                                             belkin_release();
                                             lock_serial_bus();
__CPROVER_atomic_begin();                    port_dev_registered = 0;                                             __CPROVER_atomic_end();
                                             unlock_serial_bus();
__CPROVER_atomic_begin();                    __CPROVER_assume(port_tty_registered == 0); __assume_dummy=0;        __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    dev_usb_serial_initialized = -1;                                     __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    port_initialized = 0;                                                __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    drv_module_ref_cnt = drv_module_ref_cnt - 1;                         __CPROVER_atomic_end();
                                           }
                                         }

                                         void unlock_port() {
__CPROVER_atomic_begin();                  port_lock = 0;                                                         __CPROVER_atomic_end();
                                         }

                                         void serial_write() {
__CPROVER_atomic_begin();                  assert(port_initialized);                                              __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(dev_usb_serial_initialized);                                    __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(port_tty_installed);                                            __CPROVER_atomic_end();
                                           lock_port();
__CPROVER_atomic_begin();                  assert(port_consistent);                                               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_consistent = 0;                                                   __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_consistent = 1;                                                   __CPROVER_atomic_end();
                                           unlock_port();
                                           if (write_urb_state != 0) {
                                           } else {
__CPROVER_atomic_begin();                    write_urb_state = 1;                                                 __CPROVER_atomic_end();
                                           }
                                         }

                                         void serial_bus_thread() {
                                           lock_serial_bus();
__CPROVER_atomic_begin();                  __CPROVER_assume(port_dev_registered); __assume_dummy=0;               __CPROVER_atomic_end();
                                           usb_serial_device_probe();
                                           unlock_serial_bus();
__CPROVER_atomic_begin();                  __CPROVER_assume(!port_dev_registered); __assume_dummy=0;              __CPROVER_atomic_end();
                                           lock_serial_bus();
                                           usb_serial_device_remove();
                                           unlock_serial_bus();
                                         }

                                         void cancel_work_sync() {
__CPROVER_atomic_begin();                  port_work_stop = 1;                                                    __CPROVER_atomic_end();
                                           __CPROVER_atomic_begin();
                                             __CPROVER_assume(port_work == 0); __assume_dummy=0;                  
                                           __CPROVER_atomic_end();
                                         }

                                         void lock_disc() {
                                           __CPROVER_atomic_begin();
                                             __CPROVER_assume(dev_disc_lock == 0); __assume_dummy=0;              
                                             dev_disc_lock = 1;                                                   
                                           __CPROVER_atomic_end();
                                         }

                                         void fw_module_thread() {
                                           usb_serial_init();
                                           belkin_init();
                                           __CPROVER_atomic_begin();
                                             __CPROVER_assume(drv_module_ref_cnt == 0); __assume_dummy=0;         
                                             drv_module_ref_cnt = drv_module_ref_cnt - 1;                         
                                           __CPROVER_atomic_end();
                                           belkin_exit();
                                           usb_serial_exit();
                                         }

                                         void belkin_release() {
                                         }

                                         void belkin_init() {
__CPROVER_atomic_begin();                  drv_usb_initialized = 1;                                               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  drv_usb_registered = 1;                                                __CPROVER_atomic_end();
                                           lock_table();
__CPROVER_atomic_begin();                  assert(fw_driver_list_consistent);                                     __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  fw_driver_list_consistent = 0;                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  drv_registered_with_usb_fw = 1;                                        __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  fw_driver_list_consistent = 1;                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  drv_registered_with_serial_fw = 1;                                     __CPROVER_atomic_end();
                                           unlock_table();
__CPROVER_atomic_begin();                  drv_id_table = 1;                                                      __CPROVER_atomic_end();
                                         }

                                         void belkin_exit() {
__CPROVER_atomic_begin();                  assert(drv_usb_initialized);                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  drv_registered_with_usb_fw = 0;                                        __CPROVER_atomic_end();
                                           lock_table();
__CPROVER_atomic_begin();                  assert(fw_driver_list_consistent);                                     __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  fw_driver_list_consistent = 0;                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  drv_registered_with_usb_fw = 0;                                        __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  fw_driver_list_consistent = 1;                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  drv_registered_with_serial_fw = 0;                                     __CPROVER_atomic_end();
                                           unlock_table();
__CPROVER_atomic_begin();                  drv_usb_registered = 0;                                                __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  drv_usb_initialized = 0;                                               __CPROVER_atomic_end();
                                         }

                                         void belkin_port_probe() {
                                         }

                                         void unlock_table() {
__CPROVER_atomic_begin();                  fw_table_lock = 0;                                                     __CPROVER_atomic_end();
                                         }

                                         void unlock_serial_bus() {
__CPROVER_atomic_begin();                  fw_serial_bus_lock = fw_serial_bus_lock - 1;                           __CPROVER_atomic_end();
                                         }

                                         void belkin_disconnect() {
                                         }

                                         void usb_serial_disconnect() {
                                           lock_disc();
__CPROVER_atomic_begin();                  assert(dev_usb_serial_initialized >= 0);                               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  dev_disconnected = 1;                                                  __CPROVER_atomic_end();
                                           unlock_disc();
__CPROVER_atomic_begin();                  assert(port_initialized);                                              __CPROVER_atomic_end();
                                           if (port_tty_installed == 0) {
                                           } else {
                                             __CPROVER_atomic_begin();
                                               __CPROVER_assume(fw_tty_lock == 0); __assume_dummy=0;              
                                               fw_tty_lock = 2;                                                   
                                             __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    assert(drv_module_ref_cnt > 0);                                      __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    assert(dev_usb_serial_initialized);                                  __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    assert(port_initialized);                                            __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    __CPROVER_assume(port_write_in_progress == 0); __assume_dummy=0;     __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    port_tty_installed = 0;                                              __CPROVER_atomic_end();
                                             unlock_tty();
__CPROVER_atomic_begin();                    port_work_initialized = 0;                                           __CPROVER_atomic_end();
                                             cancel_work_sync();
__CPROVER_atomic_begin();                    drv_module_ref_cnt = drv_module_ref_cnt - 1;                         __CPROVER_atomic_end();
                                             usb_serial_put();
                                             usb_serial_port_poison_urbs();
                                           }
                                           belkin_disconnect();
                                           usb_serial_put();
                                         }

                                         void usb_serial_device_probe() {
__CPROVER_atomic_begin();                  assert(port_initialized);                                              __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(dev_usb_serial_initialized >= 0);                               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  dev_autopm = dev_autopm + 1;                                           __CPROVER_atomic_end();
                                           belkin_port_probe();
__CPROVER_atomic_begin();                  port_tty_registered = 1;                                               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  dev_autopm = dev_autopm - 1;                                           __CPROVER_atomic_end();
                                         }

                                         void lock_table() {
                                           __CPROVER_atomic_begin();
                                             __CPROVER_assume(fw_table_lock == 0); __assume_dummy=0;              
                                             fw_table_lock = 1;                                                   
                                           __CPROVER_atomic_end();
                                         }

                                         void lock_serial_bus() {
                                           __CPROVER_atomic_begin();
                                             __CPROVER_assume(fw_serial_bus_lock == 0); __assume_dummy=0;         
                                             fw_serial_bus_lock = 1;                                              
                                           __CPROVER_atomic_end();
                                         }

                                         void lock_port() {
                                           __CPROVER_atomic_begin();
                                             __CPROVER_assume(port_lock == 0); __assume_dummy=0;                  
                                             port_lock = 1;                                                       
                                           __CPROVER_atomic_end();
                                         }

                                         void serial_install() {
                                           lock_table();
                                           if (port_idr_registered == 0) {
                                             unlock_table();
                                           } else {
                                             lock_disc();
                                             if (dev_disconnected == 0) {
__CPROVER_atomic_begin();                      assert(port_initialized);                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                      assert(dev_usb_serial_initialized > 0);                            __CPROVER_atomic_end();
__CPROVER_atomic_begin();                      dev_usb_serial_initialized = dev_usb_serial_initialized + 1;       __CPROVER_atomic_end();
                                               unlock_table();
                                               try_module_get();
                                               if (drv_module_ref_cnt <= 0) {
                                                 usb_serial_put();
                                                 unlock_disc();
                                               } else {
__CPROVER_atomic_begin();                        dev_autopm = dev_autopm + 1;                                     __CPROVER_atomic_end();
__CPROVER_atomic_begin();                        port_tty_installed = 1;                                          __CPROVER_atomic_end();
                                                 unlock_disc();
                                               }
                                             } else {
                                               unlock_disc();
                                               unlock_table();
                                             }
                                           }
                                         }

                                         void belkin_port_remove() {
                                         }

                                         void usb_serial_probe() {
                                           lock_table();
__CPROVER_atomic_begin();                  assert(fw_driver_list_consistent);                                     __CPROVER_atomic_end();
                                           if (drv_registered_with_usb_fw == 0) {
                                             unlock_table();
                                           } else {
                                             try_module_get();
                                             if (drv_module_ref_cnt <= 0) {
                                               unlock_table();
                                             } else {
__CPROVER_atomic_begin();                      assert(drv_usb_initialized);                                       __CPROVER_atomic_end();
__CPROVER_atomic_begin();                      assert(drv_usb_registered);                                        __CPROVER_atomic_end();
                                               unlock_table();
__CPROVER_atomic_begin();                      dev_usb_serial_initialized = 1;                                    __CPROVER_atomic_end();
                                               belkin_probe();
                                               belkin_attach();
__CPROVER_atomic_begin();                      dev_disconnected = 1;                                              __CPROVER_atomic_end();
                                               lock_table();
__CPROVER_atomic_begin();                      assert(fw_idr_consistent);                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                      fw_idr_consistent = 0;                                             __CPROVER_atomic_end();
__CPROVER_atomic_begin();                      port_idr_registered = 1;                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                      fw_idr_consistent = 1;                                             __CPROVER_atomic_end();
                                               unlock_table();
__CPROVER_atomic_begin();                      port_work_initialized = 1;                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                      port_initialized = 1;                                              __CPROVER_atomic_end();
__CPROVER_atomic_begin();                      port_dev_registered = 1;                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                      dev_disconnected = 0;                                              __CPROVER_atomic_end();
                                             }
                                           }
                                         }

                                         void try_module_get() {
                                           __CPROVER_atomic_begin();
                                             if (drv_module_ref_cnt >= 0) {
                                               drv_module_ref_cnt = drv_module_ref_cnt + 1;                       
                                             }
                                           __CPROVER_atomic_end();
                                         }

                                         void usb_serial_device_remove() {
__CPROVER_atomic_begin();                  assert(port_initialized);                                              __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert(dev_usb_serial_initialized >= 0);                               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  dev_autopm = dev_autopm + 1;                                           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_tty_registered = 0;                                               __CPROVER_atomic_end();
                                           belkin_port_remove();
__CPROVER_atomic_begin();                  dev_autopm = dev_autopm - 1;                                           __CPROVER_atomic_end();
                                         }

                                         void serial_hangup() {
                                         }

                                         void usb_serial_init() {
__CPROVER_atomic_begin();                  fw_tty_initialized = 1;                                                __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  fw_tty_registered = 1;                                                 __CPROVER_atomic_end();
                                         }

                                         void belkin_attach() {
                                         }

                                         void unlock_disc() {
__CPROVER_atomic_begin();                  dev_disc_lock = 0;                                                     __CPROVER_atomic_end();
                                         }

                                         void usb_serial_port_poison_urbs() {
                                         }

                                         void unlock_tty() {
__CPROVER_atomic_begin();                  fw_tty_lock = 0;                                                       __CPROVER_atomic_end();
                                         }

                                         void port_work_thread() {
__CPROVER_atomic_begin();                  __CPROVER_assume((port_work == 1) | (port_work_stop == 1)); __assume_dummy=0;__CPROVER_atomic_end();
__CPROVER_atomic_begin();                  assert((fw_tty_lock != 2) | (port_work_stop == 0));                    __CPROVER_atomic_end();
                                           lock_tty();
                                           if (port_work == 0) {
                                           } else {
__CPROVER_atomic_begin();                    assert(port_initialized);                                            __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    assert(port_tty_installed);                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    port_write_in_progress = 0;                                          __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    port_work = 0;                                                       __CPROVER_atomic_end();
                                           }
                                           unlock_tty();
                                         }

                                         void usb_serial_exit() {
__CPROVER_atomic_begin();                  fw_tty_registered = 0;                                                 __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  __CPROVER_assume(port_tty_installed == 0); __assume_dummy=0;           __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  fw_tty_initialized = 0;                                                __CPROVER_atomic_end();
                                         }

                                         void serial_write_callback() {
__CPROVER_atomic_begin();                  assert(port_initialized);                                              __CPROVER_atomic_end();
                                           lock_port();
__CPROVER_atomic_begin();                  assert(port_consistent);                                               __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_consistent = 0;                                                   __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_consistent = 1;                                                   __CPROVER_atomic_end();
                                           unlock_port();
__CPROVER_atomic_begin();                  assert(port_work_initialized);                                         __CPROVER_atomic_end();
__CPROVER_atomic_begin();                  port_work = 1;                                                         __CPROVER_atomic_end();
                                         }

                                         void belkin_probe() {
                                         }

                                         void attribute_thread() {
                                           try_module_get();
                                           if (drv_module_ref_cnt <= 0) {
                                           } else {
__CPROVER_atomic_begin();                    __CPROVER_assume(drv_registered_with_serial_fw); __assume_dummy=0;   __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    drv_device_id_registered = 1;                                        __CPROVER_atomic_end();
__CPROVER_atomic_begin();                    drv_module_ref_cnt = drv_module_ref_cnt - 1;                         __CPROVER_atomic_end();
                                           }
                                         }


// Line: 501