__visible long do_fast_syscall_32(struct pt_regs *regs)
{
	if (
#ifdef CONFIG_X86_64
		/*
		 * Micro-optimization: the pointer we're following is explicitly
		 * 32 bits, so it can't be out of range.
		 */
		__get_user(*(u32 *)&regs->bp,
			    (u32 __user __force *)(unsigned long)(u32)regs->sp)
#else
		get_user(*(u32 *)&regs->bp,
			 (u32 __user __force *)(unsigned long)(u32)regs->sp)
#endif
		) {

		/* User code screwed up. */
		local_irq_disable();
		regs->ax = -EFAULT;
		prepare_exit_to_usermode(regs);
		return 0;	/* Keep it simple: use IRET. */
	}

}
