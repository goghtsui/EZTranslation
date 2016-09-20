/*
 * Copyright 2016 xiaofeng gogh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gogh.plugin.common;

import com.gogh.plugin.translator.GoogleTranslator;
import com.gogh.plugin.translator.Translator;
import com.gogh.plugin.translator.YoudaoTranslator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaofeng on 9/6/2016.
 */
public class Translators {

    private static List<Translator> translators = new ArrayList<>();

    public static synchronized List<Translator> getTranslator(){
        if(translators.size() == 0){
            translators.add(new GoogleTranslator());
            translators.add(new YoudaoTranslator());
        }
        return translators;
    }

}
