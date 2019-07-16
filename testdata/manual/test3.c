if (a == 5
#ifdef CONFIG_A
	&& b == 3
#else
	&& b == 4
#endif
	) {
	;
}
