/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vincent Zhang/PhoenixLAB
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package co.phoenixlab.discord.api.util;

import static co.phoenixlab.discord.api.util.WahrDiscordApiUtils.*;
import static org.junit.Assert.*;

public class WahrDiscordApiUtilsTest {

    @org.junit.Test
    public void testAreSiblingClasses() throws Exception {
        assertTrue(areSiblingClasses(A.class, A.class));
        assertTrue(areSiblingClasses(B.class, B.class));

        assertFalse(areSiblingClasses(A.class, B.class));
        assertFalse(areSiblingClasses(B.class, A.class));

        assertTrue(areSiblingClasses(A.class, C.class));
        assertTrue(areSiblingClasses(A.class, D.class));
        assertTrue(areSiblingClasses(A.class, E.class));
        assertFalse(areSiblingClasses(A.class, F.class));

        assertFalse(areSiblingClasses(F.class, C.class));
        assertFalse(areSiblingClasses(F.class, D.class));
        assertFalse(areSiblingClasses(F.class, E.class));
    }

    class A {}
    class B {}
    class C extends A {}
    class D extends A {}
    class E extends D {}
    class F extends B {}

}
