
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


void main() {
  neh_running = 1;
  stop_neh = 0;
  rc_running = 1;
  stop_rc = 0;
  rsv_running = 1;
  stop_rsv = 0;
  shutdown_thread();
  rc_thread();
  neh_thread();
  rsv_thread();
}

void shutdown_thread() {
  stop_neh = 1;
  stop_rc = 1;
  assume(!rsv_running);
  assume(!rc_running);
  stop_rsv = 1;
  assume(!neh_running);
}

int rsv_thread() {
  assume(stop_rsv);
  rsv_running = 0;
}

int rc_thread() {
  assert(neh_running);
  assume(stop_rc);
  rc_running = 0;
}

int neh_thread() {
  assert(rsv_running);
  assume(stop_neh);
  neh_running = 0;
}

