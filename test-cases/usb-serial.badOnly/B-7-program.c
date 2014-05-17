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
  fw_tty_registered = 0;
  fw_tty_initialized = 0;
  fw_tty_lock = 0;
  fw_driver_list_consistent = 1;
  fw_idr_consistent = 1;
  fw_table_lock = 0;
  fw_serial_bus_lock = 0;
  drv_usb_registered = 0;
  drv_usb_initialized = 0;
  drv_registered_with_usb_fw = 0;
  drv_registered_with_serial_fw = 0;
  drv_id_table = 0;
  drv_module_ref_cnt = 0;
  drv_device_id_registered = 0;
  dev_usb_serial_initialized = -1;
  dev_autopm = 0;
  dev_disconnected = 0;
  dev_disc_lock = 0;
  port_dev_registered = 0;
  port_initialized = 0;
  port_tty_registered = 0;
  port_tty_installed = 0;
  port_idr_registered = 0;
  port_work = 0;
  port_work_initialized = 0;
  port_work_stop = 0;
  port_consistent = 1;
  port_lock = 0;
  port_write_in_progress = 0;
  write_urb_state = 0;
  fw_module_thread();
  usb_bus_thread();
  serial_bus_thread();
  tty_thread();
  attribute_thread();
  usb_cb_thread();
  port_work_thread();
}

void usb_bus_thread() {
  assume(drv_usb_registered | drv_device_id_registered);
  usb_serial_probe();
  if (port_dev_registered == 0) {
  } else {
    usb_serial_disconnect();
  }
}

void tty_thread() {
  assume(drv_registered_with_serial_fw);
  lock_tty();
  serial_install();
  if (port_tty_installed == 0) {
  } else {
    port_write_in_progress = 1;
    assert(port_initialized);
    assert(dev_usb_serial_initialized);
    assert(port_tty_installed);
    lock_port();
    assert(port_consistent);
    port_consistent = 0;
    port_consistent = 1;
    unlock_port();
    if (write_urb_state == 0) {
      write_urb_state = 1;
    }
  }
  unlock_tty();
}

void usb_cb_thread() {
  assume((write_urb_state == 1) | (drv_usb_registered == 0));
  if (write_urb_state == 1) {
    assert(drv_usb_initialized);
    write_urb_state = 0;
    serial_write_callback();
  }
}

void lock_tty() {
  atomicBegin();
    assume(fw_tty_lock == 0);
    fw_tty_lock = 1;
  atomicEnd();
}

void usb_serial_put() {
  int old;
  assert(dev_usb_serial_initialized > 0);
  atomicBegin();
    old = dev_usb_serial_initialized;
    dev_usb_serial_initialized = dev_usb_serial_initialized - 1;
  atomicEnd();
  if (old == 1) {
    if (port_idr_registered == 0) {
    } else {
      lock_table();
      assert(fw_idr_consistent);
      fw_idr_consistent = 0;
      port_idr_registered = 0;
      fw_idr_consistent = 1;
      unlock_table();
    }
    belkin_release();
    lock_serial_bus();
    port_dev_registered = 0;
    unlock_serial_bus();
    assume(port_tty_registered == 0);
    dev_usb_serial_initialized = -1;
    port_initialized = 0;
    drv_module_ref_cnt = drv_module_ref_cnt - 1;
  }
}

void unlock_port() {
  port_lock = 0;
}

void serial_write() {
  assert(port_initialized);
  assert(dev_usb_serial_initialized);
  assert(port_tty_installed);
  lock_port();
  assert(port_consistent);
  port_consistent = 0;
  port_consistent = 1;
  unlock_port();
  if (write_urb_state != 0) {
  } else {
    write_urb_state = 1;
  }
}

void serial_bus_thread() {
  lock_serial_bus();
  assume(port_dev_registered);
  usb_serial_device_probe();
  unlock_serial_bus();
  assume(!port_dev_registered);
  lock_serial_bus();
  usb_serial_device_remove();
  unlock_serial_bus();
}

void cancel_work_sync() {
  port_work_stop = 1;
  atomicBegin();
    assume(port_work == 0);
  atomicEnd();
}

void lock_disc() {
  atomicBegin();
    assume(dev_disc_lock == 0);
    dev_disc_lock = 1;
  atomicEnd();
}

void fw_module_thread() {
  usb_serial_init();
  belkin_init();
  atomicBegin();
    assume(drv_module_ref_cnt == 0);
    drv_module_ref_cnt = drv_module_ref_cnt - 1;
  atomicEnd();
  belkin_exit();
  usb_serial_exit();
}

void belkin_release() {
}

void belkin_init() {
  drv_usb_initialized = 1;
  drv_usb_registered = 1;
  lock_table();
  assert(fw_driver_list_consistent);
  fw_driver_list_consistent = 0;
  drv_registered_with_usb_fw = 1;
  fw_driver_list_consistent = 1;
  drv_registered_with_serial_fw = 1;
  unlock_table();
  drv_id_table = 1;
}

void belkin_exit() {
  assert(drv_usb_initialized);
  drv_registered_with_usb_fw = 0;
  lock_table();
  assert(fw_driver_list_consistent);
  fw_driver_list_consistent = 0;
  drv_registered_with_usb_fw = 0;
  fw_driver_list_consistent = 1;
  drv_registered_with_serial_fw = 0;
  unlock_table();
  drv_usb_registered = 0;
  drv_usb_initialized = 0;
}

void belkin_port_probe() {
}

void unlock_table() {
  fw_table_lock = 0;
}

void unlock_serial_bus() {
  fw_serial_bus_lock = fw_serial_bus_lock - 1;
}

void belkin_disconnect() {
}

void usb_serial_disconnect() {
  lock_disc();
  assert(dev_usb_serial_initialized >= 0);
  dev_disconnected = 1;
  unlock_disc();
  assert(port_initialized);
  if (port_tty_installed == 0) {
  } else {
    atomicBegin();
      assume(fw_tty_lock == 0);
      fw_tty_lock = 2;
    atomicEnd();
    assert(drv_module_ref_cnt > 0);
    assert(dev_usb_serial_initialized);
    assert(port_initialized);
    unlock_tty();
    assume(port_write_in_progress == 0);
    cancel_work_sync();
    port_work_initialized = 0;
    port_tty_installed = 0;
    drv_module_ref_cnt = drv_module_ref_cnt - 1;
    usb_serial_put();
    usb_serial_port_poison_urbs();
  }
  belkin_disconnect();
  usb_serial_put();
}

void usb_serial_device_probe() {
  assert(port_initialized);
  assert(dev_usb_serial_initialized >= 0);
  dev_autopm = dev_autopm + 1;
  belkin_port_probe();
  port_tty_registered = 1;
  dev_autopm = dev_autopm - 1;
}

void lock_table() {
  atomicBegin();
    assume(fw_table_lock == 0);
    fw_table_lock = 1;
  atomicEnd();
}

void lock_serial_bus() {
  atomicBegin();
    assume(fw_serial_bus_lock == 0);
    fw_serial_bus_lock = 1;
  atomicEnd();
}

void lock_port() {
  atomicBegin();
    assume(port_lock == 0);
    port_lock = 1;
  atomicEnd();
}

void serial_install() {
  lock_table();
  if (port_idr_registered == 0) {
    unlock_table();
  } else {
    lock_disc();
    if (dev_disconnected == 0) {
      assert(port_initialized);
      assert(dev_usb_serial_initialized > 0);
      dev_usb_serial_initialized = dev_usb_serial_initialized + 1;
      unlock_table();
      try_module_get();
      if (drv_module_ref_cnt <= 0) {
        usb_serial_put();
        unlock_disc();
      } else {
        dev_autopm = dev_autopm + 1;
        port_tty_installed = 1;
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
  assert(fw_driver_list_consistent);
  if (drv_registered_with_usb_fw == 0) {
    unlock_table();
  } else {
    try_module_get();
    if (drv_module_ref_cnt <= 0) {
      unlock_table();
    } else {
      assert(drv_usb_initialized);
      assert(drv_usb_registered);
      unlock_table();
      dev_usb_serial_initialized = 1;
      belkin_probe();
      belkin_attach();
      dev_disconnected = 1;
      lock_table();
      assert(fw_idr_consistent);
      fw_idr_consistent = 0;
      port_idr_registered = 1;
      fw_idr_consistent = 1;
      unlock_table();
      port_work_initialized = 1;
      port_initialized = 1;
      port_dev_registered = 1;
      dev_disconnected = 0;
    }
  }
}

void try_module_get() {
  atomicBegin();
    if (drv_module_ref_cnt >= 0) {
      drv_module_ref_cnt = drv_module_ref_cnt + 1;
    }
  atomicEnd();
}

void usb_serial_device_remove() {
  assert(port_initialized);
  assert(dev_usb_serial_initialized >= 0);
  dev_autopm = dev_autopm + 1;
  port_tty_registered = 0;
  belkin_port_remove();
  dev_autopm = dev_autopm - 1;
}

void serial_hangup() {
}

void usb_serial_init() {
  fw_tty_initialized = 1;
  fw_tty_registered = 1;
}

void belkin_attach() {
}

void unlock_disc() {
  dev_disc_lock = 0;
}

void usb_serial_port_poison_urbs() {
}

void unlock_tty() {
  fw_tty_lock = 0;
}

void port_work_thread() {
  assume((port_work == 1) | (port_work_stop == 1));
  assert((fw_tty_lock != 2) | (port_work_stop == 0));
  lock_tty();
  if (port_work == 0) {
  } else {
    assert(port_initialized);
    assert(port_tty_installed);
    port_write_in_progress = 0;
    port_work = 0;
  }
  unlock_tty();
}

void usb_serial_exit() {
  fw_tty_registered = 0;
  assume(port_tty_installed == 0);
  fw_tty_initialized = 0;
}

void serial_write_callback() {
  assert(port_initialized);
  lock_port();
  assert(port_consistent);
  port_consistent = 0;
  port_consistent = 1;
  unlock_port();
  assert(port_work_initialized);
  port_work = 1;
}

void belkin_probe() {
}

void attribute_thread() {
  try_module_get();
  if (drv_module_ref_cnt <= 0) {
  } else {
    assume(drv_registered_with_serial_fw);
    drv_device_id_registered = 1;
    drv_module_ref_cnt = drv_module_ref_cnt - 1;
  }
}

