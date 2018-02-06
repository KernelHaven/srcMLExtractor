int compare(int a, int b) {
	if (a < b) {
		return -1;
	}
	else if (b < a) {
		return 1;
	}
#ifdef WEIRD
	else if (b == a -1) {
		return 2;
	}
#endif
	else {
		return 0;
	}
}
