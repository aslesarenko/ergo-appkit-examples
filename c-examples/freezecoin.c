#include <stdlib.h>
#include <stdio.h>

#include <graal_isolate.h>
#include <libergotool.h>

int main(int argc, char **argv) {
  graal_isolate_t *isolate = NULL;
  graal_isolatethread_t *thread = NULL;
  
  if (graal_create_isolate(NULL, &isolate, &thread) != 0) {
    fprintf(stderr, "graal_create_isolate error\n");
    return 1;
  }

  // hash mock transaction to obtain id
  char * configFileName = "ergotool.json";

  // get amountToSend from cmd args and call transaction creation
  long amountToSend = atol(argv[1]);
  char result[1024 * 16];
  sendTx(thread, amountToSend, configFileName, result, sizeof(result));

  // print out serialized result
  printf("%s\n", result);

  if (graal_detach_thread(thread) != 0) {
    fprintf(stderr, "graal_detach_thread error\n");
    return 1;
  }
  
  return 0;
}
