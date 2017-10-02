enum Einum {
	E_A,
	E_B = 3,
	E_C,
};

struct Struckt {
	int a;
	char c;
};

typedef unsigned int u_int;

union Gewerkschaft {
	int a;
	float f;
};

void funcTypes(enum Einum v1, struct Struckt v2, u_int v3, union Gewerkschaft v4) {

}

void funcSizeof() {
	int a = sizeof(int);
}

void funcGoto(int a) {

	char c = 'a';
	int i = 0;
loop:

	c++;
	i++;

	if (i < a) {
		goto loop;
	}

}

void functDoWhile(int a) {

	char c = 'a';
	int i = 0;
	do {
		c++;

		if (c == 'c') {
			continue;
		}

		if (c == 'z') {
			break;
		}

		i++;
	} while (i < a);

}

void funcFor(int a) {
	char c = 'a';
	int i;
	for (i = 0; i < a; i++) {
		c++;

		if (c == 'c') {
			continue;
		}

		if (c == 'z') {
			break;
		}

	}
}

void funcWhile(int a) {
	char c = 'a';
	int i = 0;
	while (!(i < a)) {
		++c;

		if (c == 'c') {
			continue;
		}

		if (c == 'z') {
			break;
		}

		i++;
	}
}

char funcSwitch(int a) {
	char result = '\0';

	switch (a) {
	case 0:
		result = 'a';
		break;

	case 1:
	case 2:
		result = 'b';
		break;

	default:
		result = 'c';
	}

	return result;
}

char funcIf(int a) {
	char result = '\0';

	if (a == 0) {
		result = 'a';
	} else if (a == 1 || a == 2) {
		result = 'b';
	} else {
		result = 'c';
	}

	return result;
}

void funcEmptyStatement() {
	while (1);
}

void funcPointers() {
	int a = 3;
	int *pa = &a;
	int **ppa = &pa;
	*pa = 3;
	**ppa = *pa + a;
}

void funcComplexTypes() {

	int *a[4];

	int *(*(*b)[4])(int, char *[]);

	typedef int (*BinaryIntFunction)(int, int);

}

static inline void funcAttrs() {
	funcComplexTypes();
}

