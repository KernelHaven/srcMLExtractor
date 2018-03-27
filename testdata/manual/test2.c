
void func() {

    if(errno__ == INTERRUPTED ?
       false : (errno__ == EAGAIN))
        return UA_STATUSCODE_GOOD;

}


#if 0
void funcIfElse() {
    if(true) {
        int declaration;
    } else if (false) {
    	int declaration;
    } else {
        int declaration;
    }
}
#endif
