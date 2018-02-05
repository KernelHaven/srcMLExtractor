
int func(int a
#ifdef TWO_ARGS
	, int b
#endif
) {
	return a
	#ifdef TWO_ARGS
		+ b
	#endif
	;
}
