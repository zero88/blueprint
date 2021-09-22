package io.zero88.qwe.auth.mock;

import io.zero88.qwe.auth.AuthZ;

public class MockAuth {

    public static class MockAuth1 {

        @AuthZ(role = "admin")
        public boolean m1() {return true;}

        public boolean m2() {return true;}

    }


    public static class MockAuth2 extends MockAuth1 {

        @AuthZ(role = "super_user", perm = "m1")
        public boolean m1() {return true;}

        @AuthZ(perm = {"m3", "mm"})
        @AuthZ(access = "any")
        public boolean m3() {return true;}

    }


    @AuthZ(group = "happy")
    public static class MockAuthInClass {

        public boolean noop() {return true;}

    }

}
