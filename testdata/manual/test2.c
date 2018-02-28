
static enum { EMULATE, NATIVE, NONE } vsyscall_mode =
#if defined(CONFIG_LEGACY_VSYSCALL_NATIVE)
	NATIVE;
#elif defined(CONFIG_LEGACY_VSYSCALL_NONE)
	NONE;
#else
	EMULATE;
#endif
