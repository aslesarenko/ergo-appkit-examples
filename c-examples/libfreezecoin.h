#ifndef __LIBFREEZECOIN_H
#define __LIBFREEZECOIN_H

#include <graal_isolate.h>


#if defined(__cplusplus)
extern "C" {
#endif

void sendTx(graal_isolatethread_t*, ssize_t, char*, char*, size_t);

#if defined(__cplusplus)
}
#endif
#endif
