#if 0

static int pcistub_device_id_add(int domain, int bus, int slot, int func)
{
	if ((
#if !defined(MODULE) /* pci_domains_supported is not being exported */ \
    || !defined(CONFIG_PCI_DOMAINS)
	     !pci_domains_supported ? domain :
#endif
	     domain < 0 || domain > 0xffff)
	    || bus < 0 || bus > 0xff)
		return -EINVAL;

	return 0;
}

#endif

static void func() {

	if (a <
		#ifdef CONFIG_A
			2
		#else
			4
		#endif
	) {
		;
	}

}
