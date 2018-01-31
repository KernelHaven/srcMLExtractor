#include <something.h>

struct adresse {
	char name[50];
	char strasse[100];
	short hausnummer;
	long plz;
	char stadt[50];
};

void function(
#ifdef A
int a,
#endif
int b) {
    int y = 2;
    y +=
    #ifdef A
        3
    #else
        5
    #endif
    ;
    func(y);
    
    if (y<2) {
        while(y>0) {
            y++;
        }
    } else {
        for (int a; a < y; a++) {
            a += y;
            continue;
        }
    }
    
    switch (y) {
        case 1:
            y % 2;
            break;
        default:
            y *= y;
    }
    
    {
        char[] a;
    }
}
