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

import co.phoenixlab.discord.api.exceptions.RateLimitExceededException;
import lombok.Getter;

import java.util.Arrays;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RateLimiter {

    @Getter
    private final String label;
    private final ReadWriteLock lock;
    @Getter
    private int maxCharges;
    @Getter
    private long periodMs;
    private long[] charges;

    public RateLimiter(long periodMs, int maxCharges) {
        this("", periodMs, maxCharges);
    }

    public RateLimiter(String label, long periodMs, int maxCharges) {
        this.label = label;
        this.periodMs = periodMs;
        this.maxCharges = maxCharges;
        this.charges = new long[maxCharges];
        this.lock = new ReentrantReadWriteLock();
    }

    public void mark() throws RateLimitExceededException {
        try {
            //  Will never throw InterruptedException with false param
            mark(false);
        } catch (InterruptedException ignore) {
            //  ignore
        }
    }

    public long tryMark() {
        try {
            //  Will never throw InterruptedException with false param
            mark(false);
            return 0L;
        } catch (InterruptedException ignore) {
            //  ignore
            return 0L;
        } catch (RateLimitExceededException e) {
            return e.getRetryIn();
        }
    }

    public void mark(boolean waitFor) throws RateLimitExceededException, InterruptedException {
        long now = System.currentTimeMillis();
        long diff;
        try {
            lock.readLock().lock();
            do {
                diff = Long.MAX_VALUE;
                for (int i = 0; i < charges.length; i++) {
                    long delta = now - charges[i];
                    if (delta >= periodMs) {
                        lock.readLock().unlock();
                        try {
                            lock.writeLock().lock();
                            //  recheck
                            if (delta >= now - charges[i]) {
                                charges[i] = now;
                                return;
                            }
                            //  if not we go on to the next one
                        } finally {
                            lock.writeLock().unlock();
                        }
                    }
                    if (delta < diff) {
                        diff = delta;
                    }
                }
                if (waitFor) {
                    Thread.yield();
                    Thread.sleep(100);
                }
            } while (waitFor);
        } finally {
            lock.readLock().unlock();
        }
        throw new RateLimitExceededException(label, diff);
    }

    public int getRemainingCharges() {
        int count = 0;
        try {
            lock.readLock().lock();
            for (long charge : charges) {
                if (isTimeOnCd(charge)) {
                    ++count;
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return maxCharges - count;
    }

    public boolean hasCharges() {
        return getRemainingCharges() > 0;
    }

    private boolean isChargeOnCd(int charge) {
        return charges[charge] != 0 && isTimeOnCd(charges[charge]);
    }

    private boolean isTimeOnCd(long time) {
        return System.currentTimeMillis() - time < periodMs;
    }

    public void reset() {
        try {
            lock.writeLock().lock();
            Arrays.fill(charges, 0);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String toString() {
        return String.format("RateLimiter %s: period:%,dms chargeCount:%,d charges:%s",
                label, periodMs, maxCharges, Arrays.toString(charges));
    }
}
