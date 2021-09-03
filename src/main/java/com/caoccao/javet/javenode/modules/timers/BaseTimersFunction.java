/*
 * Copyright (c) 2021. caoccao.com Sam Cao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.caoccao.javet.javenode.modules.timers;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.modules.BaseJNFunction;
import com.caoccao.javet.javenode.utils.V8ValueExUtils;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueFunction;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

public abstract class BaseTimersFunction extends BaseJNFunction {
    protected int delay;
    protected Disposable disposable;
    protected boolean recurrent;
    protected V8Value[] v8ValueArgs;
    protected V8ValueFunction v8ValueFunctionCallback;

    public BaseTimersFunction(
            JNEventLoop eventLoop,
            boolean recurrent,
            V8ValueFunction v8ValueFunctionCallback,
            int delay,
            V8Value... v8ValueArgs) throws JavetException {
        super(eventLoop);
        this.recurrent = recurrent;
        this.v8ValueArgs = V8ValueExUtils.toClone(v8ValueArgs);
        this.delay = delay;
        this.v8ValueFunctionCallback = v8ValueFunctionCallback.toClone();
        disposable = null;
    }

    @Override
    public void close() throws JavetException {
        if (!isClosed()) {
            if (hasRef()) {
                disposable.dispose();
                if (!recurrent) {
                    eventLoop.decrementBlockingEventCount();
                }
                disposable = null;
            }
            JavetResourceUtils.safeClose(v8ValueFunctionCallback);
            v8ValueFunctionCallback = null;
            JavetResourceUtils.safeClose((Object[]) v8ValueArgs);
            v8ValueArgs = null;
        }
    }

    public boolean hasRef() {
        return disposable != null && !disposable.isDisposed();
    }

    @Override
    public boolean isClosed() {
        return JavetResourceUtils.isClosed(v8ValueFunctionCallback);
    }

    public V8Value ref(V8Value thisObject) {
        return thisObject;
    }

    @Override
    public void run() {
        Scheduler scheduler = Schedulers.from(eventLoop.getExecutorService());
        if (recurrent) {
            disposable = Observable.interval(delay, TimeUnit.MILLISECONDS, scheduler)
                    .subscribe(t -> {
                        if (!isClosed()) {
                            v8ValueFunctionCallback.call(null, v8ValueArgs);
                        }
                    });
        } else {
            eventLoop.incrementBlockingEventCount();
            disposable = Observable.timer(delay, TimeUnit.MILLISECONDS, scheduler)
                    .subscribe(t -> {
                        try {
                            if (!isClosed()) {
                                v8ValueFunctionCallback.call(null, v8ValueArgs);
                            }
                        } finally {
                            eventLoop.decrementBlockingEventCount();
                        }
                    });
        }
    }

    public V8Value unref(V8Value thisObject) {
        if (hasRef()) {
            disposable.dispose();
            if (!recurrent) {
                eventLoop.decrementBlockingEventCount();
            }
            disposable = null;
        }
        return thisObject;
    }
}
