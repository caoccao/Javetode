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

package com.caoccao.javet.javenode.modules.v8.timers;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.exceptions.JavetExecutionException;
import com.caoccao.javet.javenode.BaseTestJavenodeSuite;
import com.caoccao.javet.values.reference.V8ValueArray;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class TestTimersTimeout extends BaseTestJavenodeSuite {
    @Test
    public void testInvalidArgumentCount() throws JavetException {
        try (TimersModule timersModule = new TimersModule(v8Runtime)) {
            timersModule.bind(v8Runtime.getGlobalObject());
            v8Runtime.getExecutor("setTimeout();").executeVoid();
            fail("Failed to throw exception");
        } catch (JavetExecutionException e) {
            assertEquals("Error: setTimeout() takes a least 1 argument", e.getMessage());
        }
    }

    @Test
    public void testInvalidCallback() throws JavetException {
        try (TimersModule timersModule = new TimersModule(v8Runtime)) {
            timersModule.bind(v8Runtime.getGlobalObject());
            v8Runtime.getExecutor("setTimeout(1);").executeVoid();
            fail("Failed to throw exception");
        } catch (JavetExecutionException e) {
            assertEquals("Error: Argument [callback] must be a function", e.getMessage());
        }
    }

    @Test
    public void testInvalidDelayNegativeInteger() throws JavetException {
        try (TimersModule timersModule = new TimersModule(v8Runtime)) {
            timersModule.bind(v8Runtime.getGlobalObject());
            v8Runtime.getExecutor("setTimeout(() => {}, -1);").executeVoid();
            fail("Failed to throw exception");
        } catch (JavetExecutionException e) {
            assertEquals("Error: Argument [delay] must be a positive integer", e.getMessage());
        }
    }

    @Test
    public void testInvalidDelayString() throws JavetException {
        try (TimersModule timersModule = new TimersModule(v8Runtime)) {
            timersModule.bind(v8Runtime.getGlobalObject());
            v8Runtime.getExecutor("setTimeout(() => {}, 'a');").executeVoid();
            fail("Failed to throw exception");
        } catch (JavetExecutionException e) {
            assertEquals("Error: Argument [delay] must be an integer", e.getMessage());
        }
    }

    @Test
    public void testRef() throws JavetException {
        try (TimersModule timersModule = new TimersModule(v8Runtime)) {
            timersModule.bind(v8Runtime.getGlobalObject());
            String jsonString = v8Runtime.getExecutor(
                    "const a = [];\n" +
                            "const t = setTimeout(() => {}, 10000);\n" +
                            "a.push(t.hasRef());\n" +
                            "a.push(t.refresh() == t);\n" +
                            "a.push(t.ref() == t);\n" +
                            "a.push(t.unref() == t);\n" +
                            "a.push(t.hasRef());\n" +
                            "JSON.stringify(a);").executeString();
            assertEquals("[true,true,true,true,false]", jsonString);
        }
    }

    @Test
    public void testRx() throws JavetException, InterruptedException {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Observable.timer(10, TimeUnit.MILLISECONDS, Schedulers.from(executorService))
                .subscribe(t -> atomicBoolean.set(true));
        executorService.awaitTermination(100, TimeUnit.MILLISECONDS);
        assertTrue(atomicBoolean.get());
    }

    @Test
    public void testWithDelayAndArgs() throws JavetException, InterruptedException {
        try (TimersModule timersModule = new TimersModule(v8Runtime)) {
            timersModule.bind(v8Runtime.getGlobalObject());
            v8Runtime.getExecutor("var a = [];" +
                    "setTimeout((b) => {\n" +
                    "  a.push(b);\n" +
                    "}, 20, 2);\n" +
                    "a.push(1);").executeVoid();
            boolean testPassed = false;
            for (int i = 0; i < 100; ++i) {
                TimeUnit.MILLISECONDS.sleep(1);
                try (V8ValueArray v8ValueArray = v8Runtime.getGlobalObject().get("a")) {
                    if (v8ValueArray.getLength() == 2) {
                        assertEquals("[1,2]", v8ValueArray.toJsonString());
                        assertTrue(i > 5);
                        testPassed = true;
                        break;
                    }
                }
            }
            assertTrue(testPassed);
        }
    }

    @Test
    public void testWithDelayWithoutArgs() throws JavetException, InterruptedException {
        try (TimersModule timersModule = new TimersModule(v8Runtime)) {
            timersModule.bind(v8Runtime.getGlobalObject());
            v8Runtime.getExecutor("var a = [];" +
                    "setTimeout(() => {\n" +
                    "  a.push(2);\n" +
                    "}, 20);\n" +
                    "a.push(1);").executeVoid();
            boolean testPassed = false;
            for (int i = 0; i < 100; ++i) {
                TimeUnit.MILLISECONDS.sleep(1);
                try (V8ValueArray v8ValueArray = v8Runtime.getGlobalObject().get("a")) {
                    if (v8ValueArray.getLength() == 2) {
                        assertEquals("[1,2]", v8ValueArray.toJsonString());
                        assertTrue(i > 5);
                        testPassed = true;
                        break;
                    }
                }
            }
            assertTrue(testPassed);
        }
    }
}
