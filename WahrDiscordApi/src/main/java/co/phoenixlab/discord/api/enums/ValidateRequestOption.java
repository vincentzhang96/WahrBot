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

package co.phoenixlab.discord.api.enums;

import org.hibernate.validator.internal.util.IdentitySet;

import java.util.Collections;

public class ValidateRequestOption {

    public static final ValidateRequestOption REQUEST_CAN_BE_NULL = new ValidateRequestOption();
    public static final ValidateRequestOption REQUEST_MUST_BE_NULL = new ValidateRequestOption();

    private ValidateRequestOption(){}

    public static boolean test(ValidateRequestOption[] options, ValidateRequestOption forThis) {
        if (options.length == 0) {
            return false;
        }
        if (options.length == 1) {
            return options[0] == forThis;
        }
        IdentitySet set = new IdentitySet();
        Collections.addAll(set, options);
        return set.contains(forThis);
    }

    public static boolean doIf(ValidateRequestOption[] options, ValidateRequestOption forThis, Runnable r) {
        if (test(options, forThis)) {
            r.run();
            return true;
        }
        return false;
    }

    public static boolean doIfOtherwise(ValidateRequestOption[] options, ValidateRequestOption forThis,
                                        Runnable doIf, Runnable otherwise) {
        if (!doIf(options, forThis, doIf)) {
            otherwise.run();
            return false;
        }
        return true;
    }

    public static ValidateChain chain(ValidateRequestOption[] options) {
        ValidateChain ret = new ValidateChain();
        ret.set = new IdentitySet(options.length);
        Collections.addAll(ret.set, options);
        return ret;
    }

    public static class ValidateChain {
        private IdentitySet set;
        private boolean active = true;
        private boolean hit = false;

        public ValidateChain doIf(ValidateRequestOption option, Runnable runnable) {
            if (active) {
                if (set.contains(option)) {
                    runnable.run();
                    hit = true;
                }
            }
            return this;
        }

        public ValidateChain doIfAndEnd(ValidateRequestOption option, Runnable runnable) {
            if (active) {
                if (set.contains(option)) {
                    runnable.run();
                    hit = true;
                    active = false;
                }
            }
            return this;
        }

        public void elseDo(Runnable runnable) {
            if (active && !hit) {
                runnable.run();
                hit = true;
            }
        }
    }
}
