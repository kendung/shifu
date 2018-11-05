/*
 * Copyright [2012-2018] PayPal Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ml.shifu.shifu.core;

import ml.shifu.shifu.container.obj.GenericModelConfig;
import org.encog.ml.data.MLData;
import java.util.Map;

/**
 * 
 * @author minizhuwei
 * This interface is used to extend shifu evaluation capability.
 * The foreign formated model evaluator should implement this interface
 * with single process evaluation logic in compute method
 */
public interface Computable {

    public void init(GenericModelConfig config);

    public double compute(MLData input); 

    public void releaseResource();
}
