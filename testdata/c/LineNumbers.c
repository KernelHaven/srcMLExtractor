#include <stdio.h>

int main(int argc, char **argv) {
#ifdef CONFIG_DEBUG
    printf("Debugging");
#endif
    return 0;
}
