#ifndef __LIBERGOTOOL_H
#define __LIBERGOTOOL_H

#include <graal_isolate.h>


#if defined(__cplusplus)
extern "C" {
#endif

void aggregateBoxes(graal_isolatethread_t*, char*, char*, size_t);

void sendTx(graal_isolatethread_t*, ssize_t, char*, char*, size_t);

void hashBlake2b256(graal_isolatethread_t*, char*, char*, size_t);

void prepareBox(graal_isolatethread_t*, char*, char*, char*, size_t);

#if defined(__cplusplus)
}
#endif
#endif
