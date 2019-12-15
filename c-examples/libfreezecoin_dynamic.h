#ifndef __LIBFREEZECOIN_H
#define __LIBFREEZECOIN_H

#include <graal_isolate_dynamic.h>


#if defined(__cplusplus)
extern "C" {
#endif

typedef void (*sendTx_fn_t)(graal_isolatethread_t*, ssize_t, char*, char*, size_t);

#if defined(__cplusplus)
}
#endif
#endif
