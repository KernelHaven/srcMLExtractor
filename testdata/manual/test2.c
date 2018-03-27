#include <stdioh.h>
#include "test2.h"

#ifdef A

void someFunc() {
	printf("someFunc()\n");
}


int otherFunc(int a, int b) {
	return a + b;
}

#endif
