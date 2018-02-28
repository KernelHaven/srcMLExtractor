static struct addr_marker address_markers[] = {
	[VMEMMAP_START_NR]	= { 0UL,		"Vmemmap" },
#ifdef CONFIG_KASAN
	[KASAN_SHADOW_END_NR]	= { KASAN_SHADOW_END,	"KASAN shadow end" },
#endif
	[CPU_ENTRY_AREA_NR]	= { CPU_ENTRY_AREA_BASE,"CPU entry Area" },
};
