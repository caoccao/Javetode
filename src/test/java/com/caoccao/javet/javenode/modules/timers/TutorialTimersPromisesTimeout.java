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
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.enums.JNModuleType;

public class TutorialTimersPromisesTimeout {
    public static void main(String[] args) throws JavetException, InterruptedException {
        try (V8Runtime v8Runtime = V8Host.getV8Instance().createV8Runtime();
             JNEventLoop eventLoop = new JNEventLoop(v8Runtime)) {
            eventLoop.loadStaticModules(JNModuleType.Console);
            eventLoop.registerDynamicModules(JNModuleType.TimersPromises);
            v8Runtime.getExecutor(
                    "import { setTimeout } from 'timers/promises';\n" +
                            "const a = [];\n" +
                            "setTimeout(10, 'Hello Javenode')\n" +
                            "  .then(result => a.push(result));\n" +
                            "globalThis.a = a;").setModule(true).executeVoid();
            eventLoop.await();
            v8Runtime.getExecutor("console.log(a[0]);").executeVoid();
        }
    }
}
