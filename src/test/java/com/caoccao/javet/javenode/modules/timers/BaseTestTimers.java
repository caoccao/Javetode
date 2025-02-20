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
import com.caoccao.javet.javenode.BaseJNTestSuite;
import com.caoccao.javet.javenode.enums.JNModuleType;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseTestTimers extends BaseJNTestSuite {
    protected long originalDefaultDelay;

    @Override
    public void afterEach() throws JavetException {
        super.afterEach();
        TimersConstants.DEFAULT_DELAY = originalDefaultDelay;
    }

    @BeforeEach
    @Override
    public void beforeEach() throws JavetException {
        super.beforeEach();
        assertEquals(1, eventLoop.loadStaticModules(JNModuleType.Timers));
        originalDefaultDelay = TimersConstants.DEFAULT_DELAY;
        TimersConstants.DEFAULT_DELAY = 100;
    }
}
