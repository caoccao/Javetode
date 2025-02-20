/*
 * Copyright (c) 2021-2025. caoccao.com Sam Cao
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
import com.caoccao.javet.exceptions.JavetExecutionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestTimersTimeout extends BaseTestTimers {
    @Test
    public void testClearTimeout() throws JavetException {
        v8Runtime.getExecutor("const a = [];\n" +
                "var t = setTimeout(() => { a.push(true); }, 1000);\n" +
                "clearTimeout(t);").executeVoid();
        assertEquals(0, eventLoop.getBlockingEventCount());
        assertFalse(v8Runtime.getExecutor("t.hasRef()").executeBoolean());
        assertEquals("[]", v8Runtime.getExecutor("JSON.stringify(a);").executeString());
        v8Runtime.getExecutor("t = undefined;").executeVoid();
    }

    @Test
    public void testInvalidArgumentCount() throws JavetException {
        try {
            v8Runtime.getExecutor("setTimeout();").executeVoid();
            fail("Failed to throw exception");
        } catch (JavetExecutionException e) {
            assertEquals("Error: setTimeout() takes a least 1 argument", e.getMessage());
        }
    }

    @Test
    public void testInvalidCallback() throws JavetException {
        try {
            v8Runtime.getExecutor("setTimeout(1);").executeVoid();
            fail("Failed to throw exception");
        } catch (JavetExecutionException e) {
            assertEquals("Error: Argument [callback] must be a function", e.getMessage());
        }
    }

    @Test
    public void testInvalidDelayNegativeNumber() throws JavetException {
        try {
            v8Runtime.getExecutor("setTimeout(() => {}, -1);").executeVoid();
            fail("Failed to throw exception");
        } catch (JavetExecutionException e) {
            assertEquals("Error: Argument [delay] must be a positive number", e.getMessage());
        }
    }

    @Test
    public void testInvalidDelayString() throws JavetException {
        try {
            v8Runtime.getExecutor("setTimeout(() => {}, 'a');").executeVoid();
            fail("Failed to throw exception");
        } catch (JavetExecutionException e) {
            assertEquals("Error: Argument [delay] must be a number", e.getMessage());
        }
    }

    @Test
    public void testRef() throws JavetException {
        v8Runtime.getExecutor("const a = [];\n" +
                "var t = setTimeout(() => {}, 10000);").executeVoid();
        assertEquals(1, eventLoop.getBlockingEventCount());
        v8Runtime.getExecutor("a.push(t.hasRef());").executeVoid();
        assertEquals(1, eventLoop.getBlockingEventCount());
        v8Runtime.getExecutor("a.push(t.refresh() == t);").executeVoid();
        assertEquals(1, eventLoop.getBlockingEventCount());
        v8Runtime.getExecutor("a.push(t.ref() == t);").executeVoid();
        assertEquals(1, eventLoop.getBlockingEventCount());
        v8Runtime.getExecutor("a.push(t.unref() == t);").executeVoid();
        assertEquals(0, eventLoop.getBlockingEventCount());
        v8Runtime.getExecutor("a.push(t.hasRef());").executeVoid();
        assertEquals(0, eventLoop.getBlockingEventCount());
        String jsonString = v8Runtime.getExecutor("JSON.stringify(a);").executeString();
        assertEquals(0, eventLoop.getBlockingEventCount());
        assertEquals("[true,true,true,true,false]", jsonString);
        v8Runtime.getExecutor("t = undefined;").executeVoid();
    }

    @Test
    public void testToPrimitive() throws JavetException, InterruptedException {
        int referenceId = v8Runtime.getExecutor("var t = setTimeout(() => {\n" +
                "}, 1000);\n" +
                "const referenceId = t[Symbol.toPrimitive]();\n" +
                "t.unref();\n" +
                "referenceId;").executeInteger();
        assertTrue(referenceId > 0);
        assertTrue(TimersTimeout.getGlobalReferenceId() >= referenceId);
        v8Runtime.getExecutor("t = undefined;").executeVoid();
    }

    @Test
    public void testWithDelayAndArgs() throws JavetException, InterruptedException {
        v8Runtime.getExecutor("const a = [];" +
                "var t = setTimeout((b) => {\n" +
                "  a.push(b);\n" +
                "}, 10, 2);\n" +
                "a.push(1);").executeVoid();
        assertEquals("[1]", v8Runtime.getExecutor("JSON.stringify(a);").executeString());
        assertEquals(1, eventLoop.getBlockingEventCount());
        eventLoop.await();
        assertFalse(v8Runtime.getExecutor("t.hasRef()").executeBoolean());
        assertEquals("[1,2]", v8Runtime.getExecutor("JSON.stringify(a);").executeString());
        assertEquals(0, eventLoop.getBlockingEventCount());
        v8Runtime.getExecutor("t = undefined;").executeVoid();
    }

    @Test
    public void testWithDelayWithoutArgs() throws JavetException, InterruptedException {
        v8Runtime.getExecutor("const a = [];" +
                "var t = setTimeout(() => {\n" +
                "  a.push(2);\n" +
                "}, 10);\n" +
                "a.push(1);").executeVoid();
        assertEquals("[1]", v8Runtime.getExecutor("JSON.stringify(a);").executeString());
        assertEquals(1, eventLoop.getBlockingEventCount());
        eventLoop.await();
        assertFalse(v8Runtime.getExecutor("t.hasRef()").executeBoolean());
        assertEquals("[1,2]", v8Runtime.getExecutor("JSON.stringify(a);").executeString());
        assertEquals(0, eventLoop.getBlockingEventCount());
        v8Runtime.getExecutor("t = undefined;").executeVoid();
    }
}
