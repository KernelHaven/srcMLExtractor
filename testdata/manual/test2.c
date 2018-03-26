void func() {

    if(errno__ == INTERRUPTED ?
       false : (errno__ == EAGAIN))
        return UA_STATUSCODE_GOOD;

}
