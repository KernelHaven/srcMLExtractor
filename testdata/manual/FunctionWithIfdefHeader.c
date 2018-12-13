long calc(
#ifdef LONG
	long
#else
	int
#endif
a) {
	return 2 * a;
}
