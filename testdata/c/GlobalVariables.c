// Struct-based variables:
struct adresse {
	char name[50];
	char strasse[100];
	short hausnummer;
	long plz;
	char stadt[50];
} adr;

// See https://stackoverflow.com/a/12644162
struct {
    int a;
    int b;
} x;

// Single line statements with initialization
// See https://en.wikipedia.org/wiki/Global_variable#C_and_C++
static int shared = 3;
extern int overShared = 1;
int overSharedToo = 2;

// Single line statements without initialization
static int shared2;
