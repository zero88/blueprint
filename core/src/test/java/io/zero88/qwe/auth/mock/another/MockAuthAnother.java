package io.zero88.qwe.auth.mock.another;

import io.zero88.qwe.auth.AuthN;
import io.zero88.qwe.auth.AuthZ;

@AuthN
public class MockAuthAnother {

    public boolean access() {return true;}

    @AuthZ(access = "any")
    public boolean create() {return true;}

}
