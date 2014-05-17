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


void main() {
  registered = 0;
  netif_running = 0;
  irq_enabled = 0;
  napi_enabled = 0;
  napi_scheduled = 0;
  reset_task_ready = 0;
  sysfs_registered = 0;
  dev_lock = 0;
  sysfs_lock = 0;
  rtnl_lock = 0;
  want_sysfs = 0;
  want_dev = 0;
  irq_sem = 0;
  napi_sem = 0;
  dev_on = 0;
  IntrStatus = 0;
  IntrMask = 0;
  pci_thread();
  network_thread();
  irq_thread();
  napi_thread();
  workqueue_thread();
  sysfs_thread();
  dev_thread();
  deadlock_thread();
}

void drv_xmit_timeout() {
  reset_task_ready = 1;
}

void napi_disable() {
  napi_enabled = 0;
  down(napi_sem);
}

void network_thread() {
  int open1;
  while (1) {
    open1 = open();
    if (open1 == 0) {
      if (nondet == 0) {
        drv_xmit_timeout();
      } else {
        drv_start_xmit();
      }
      close();
    }
  }
}

void napi_thread() {
  while (1) {
    down(napi_sem);
    if (napi_scheduled == 0) {
    } else {
      drv_napi_poll();
    }
    up(napi_sem);
  }
}

void sysfs_thread() {
  int nondet1;
  while (1) {
    noReorderBegin();
      want_sysfs = 6;
      atomicBegin();
        assume(sysfs_lock == 0);
        sysfs_lock = 6;
        want_sysfs = 0;
      atomicEnd();
    noReorderEnd();
    if (sysfs_registered != 0) {
      nondet1 = nondet_int();
      drv_sysfs_read(nondet1);
    }
    unlock(sysfs_lock);
    noReorderBegin();
      want_dev = 6;
      atomicBegin();
        assume(dev_lock == 0);
        dev_lock = 6;
        want_dev = 0;
      atomicEnd();
    noReorderEnd();
    unlock(dev_lock);
  }
}

void napi_complete() {
  napi_scheduled = 0;
}

void workqueue_thread() {
  while (1) {
    if (reset_task_ready == 0) {
    } else {
      drv_reset_task();
      reset_task_ready = 0;
    }
  }
}

void remove_sysfs_files() {
  noReorderBegin();
    want_sysfs = 5;
    atomicBegin();
      assume(sysfs_lock == 0);
      sysfs_lock = 5;
      want_sysfs = 0;
    atomicEnd();
  noReorderEnd();
  device_remove_bin_file();
  unlock(sysfs_lock);
}

int drv_irq() {
  int handled2;
  u8 status1;
  handled2 = 0;
  status1 = IntrStatus;
  while (nondet != 0) {
    write_IntrStatus(status1);
    if (intr_mask != 0) {
      intr_mask = 0;
      write_IntrMask(0);
      napi_schedule();
      handled2 = 1;
    }
    status1 = IntrStatus;
  }
  assume(!status1);
  return handled2;
}

void drv_reset_task() {
  lock(rtnl_lock);
  if ((!netif_running) == 0) {
    napi_disable();
    dev_down();
    dev_up();
    napi_enable();
    unlock(rtnl_lock);
  }
}

int close() {
  lock(rtnl_lock);
  if (netif_running != 0) {
    drv_close();
    netif_running = 0;
  }
  unlock(rtnl_lock);
}

int open() {
  int ret1;
  ret1 = 0;
  if (registered != 0) {
    ret1 = drv_open();
    if (ret1 == 0) {
      netif_running = 1;
    }
  }
  lock(rtnl_lock);
  unlock(rtnl_lock);
  return ret1;
}

int drv_open() {
  int rc2;
  int ret5;
  rc2 = request_irq();
  IntrMask = 0;
  if (rc2 < 0) {
    ret5 = rc2;
  } else {
    napi_enable();
    dev_up();
    ret5 = 0;
  }
  (*hw_start)();
  return ret5;
}

void free_irq() {
  irq_enabled = 0;
}

void unregister_netdev() {
  registered = 0;
  while (netif_running != 0) {}
}

void device_create_bin_file() {
  sysfs_registered = 1;
}

void drv_napi_poll() {
  int work_done;
  work_done = handle_interrupt();
  if (work_done < 100) {
    napi_complete();
    IntrMask = 255;
    intr_mask = 255;
  }
}

void drv_hw_start() {
}

void drv_start_xmit() {
}

void drv_disconnect() {
  remove_sysfs_files();
  unregister_netdev();
}

void pci_thread() {
  int probe1;
  probe1 = drv_probe();
  if (probe1 == 0) {
    drv_disconnect();
    noReorderBegin();
      want_dev = 5;
      atomicBegin();
        assume(dev_lock == 0);
        dev_lock = 5;
        want_dev = 0;
      atomicEnd();
    noReorderEnd();
    unlock(dev_lock);
  }
}

int handle_interrupt() {
  int nondet3;
  nondet3 = nondet_int();
  IntrStatus = 0;
  return nondet3;
}

void napi_schedule() {
  atomicBegin();
    if (napi_enabled != 0) {
      napi_scheduled = 1;
    }
  atomicEnd();
}

void write_IntrStatus(u8 val) {
  assert(dev_on != 0);
  IntrStatus = IntrStatus & (~val);
}

void device_remove_bin_file() {
  sysfs_registered = 0;
}

int register_netdev() {
  int ret2;
  if (nondet == 0) {
    ret2 = -1;
  } else {
    registered = 1;
    ret2 = 0;
  }
  return ret2;
}

void dev_down() {
  write_IntrMask(0);
  synchronize_irq();
  dev_on = 0;
}

void napi_enable() {
  napi_enabled = 1;
}

void dev_up() {
  dev_on = 1;
  intr_mask = 1;
  write_IntrMask(1);
}

void dev_thread() {
  while (1) {
    IntrStatus = 1;
  }
}

int drv_probe() {
  int rc1;
  int ret4;
  create_sysfs_files();
  hw_start = drv_hw_start;
  rc1 = register_netdev();
  if (rc1 < 0) {
    ret4 = rc1;
  } else {
    ret4 = 0;
  }
  return ret4;
}

void drv_close() {
  napi_disable();
  free_irq();
  dev_down();
}

void write_IntrMask(u8 val) {
  assert(dev_on != 0);
  IntrMask = val;
}

void synchronize_irq() {
  down(irq_sem);
  up(irq_sem);
}

int request_irq() {
  int ret3;
  if (nondet == 0) {
    ret3 = -1;
  } else {
    irq_enabled = 1;
    ret3 = 0;
  }
  return ret3;
}

void create_sysfs_files() {
  lock(sysfs_lock);
  device_create_bin_file();
  unlock(sysfs_lock);
}

void irq_thread() {
  while (1) {
    down(irq_sem);
    up(irq_sem);
    if ((((irq_enabled != 0) & (IntrStatus != 0)) & (IntrMask != 0)) == 0) {
    } else {
      assert(intr_mask != 0);
      drv_irq();
    }
  }
}

unsigned int drv_sysfs_read(int off) {
  int nondet2;
  nondet2 = nondet_int();
  return nondet2;
}

void deadlock_thread() {
  assert(!((((dev_lock == want_sysfs) & (sysfs_lock == want_dev)) & (dev_lock != 0)) & (sysfs_lock != 0)));
}

