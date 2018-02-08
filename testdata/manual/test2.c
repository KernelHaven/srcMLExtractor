enum {
	arg_DI = 7,
#ifdef __amd64__
	arg_R8  = 8,
#endif
};

struct A {
	int a;
	char b;
};

typedef unsgigned int u32;

typedef struct {
	int c;
	char d;
} STRUCKT;
