package ml.shifu.shifu.util;

import ml.shifu.shifu.column.NSColumn;
import ml.shifu.shifu.container.obj.ColumnConfig;
import ml.shifu.shifu.container.obj.ModelConfig;
import ml.shifu.shifu.core.Normalizer;
import ml.shifu.shifu.core.dtrain.CommonConstants;
import org.apache.commons.collections.CollectionUtils;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;

import java.util.*;

/**
 * Copyright [2013-2018] PayPal Software Foundation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

public class NormalUtils {

    /**
     * Assemble map data to Encog standard input format. If no variable selected(noVarSel = true), all candidate
     * variables will be selected.
     *
     * @param binCategoryMap   categorical map
     * @param noVarSel         if after var select
     * @param modelConfig      model config instance
     * @param columnConfigList column config list
     * @param rawNsDataMap     raw NSColumn data
     * @param cutoff           cut off value
     * @param alg              algorithm used in model
     * @return data pair instance
     * @throws NullPointerException  if input is null
     * @throws NumberFormatException if column value is not number format.
     */
    public static MLDataPair assembleNsDataPair(Map<Integer, Map<String, Integer>> binCategoryMap, boolean noVarSel,
            ModelConfig modelConfig, List<ColumnConfig> columnConfigList, Map<NSColumn, String> rawNsDataMap,
            double cutoff, String alg) {
        double[] ideal = { Constants.DEFAULT_IDEAL_VALUE };

        List<Double> inputList = new ArrayList<Double>();
        boolean hasCandidates = CommonUtils.hasCandidateColumns(columnConfigList);
        for(ColumnConfig config : columnConfigList) {
            if(config == null) {
                continue;
            }
            NSColumn key = new NSColumn(config.getColumnName());
            if(config.isFinalSelect() // check whole name
                    && !rawNsDataMap.containsKey(key) // and then check simple name, in case user use wrong namespace
                    && !rawNsDataMap.containsKey(new NSColumn(key.getSimpleName()))) {
                throw new IllegalStateException(String.format("Variable Missing in Test Data: %s", key));
            }

            if(config.isTarget()) {
                continue;
            } else {
                if(!noVarSel) {
                    if(config != null && !config.isMeta() && !config.isTarget() && config.isFinalSelect()) {
                        String val = getNSVariableVal(rawNsDataMap, key);
                        if(CommonUtils.isTreeModel(alg) && config.isCategorical()) {
                            Integer index = binCategoryMap.get(config.getColumnNum()).get(val == null ? "" : val);
                            if(index == null) {
                                // not in binCategories, should be missing value
                                // -1 as missing value
                                inputList.add(-1d);
                            } else {
                                inputList.add(index * 1d);
                            }
                        } else {
                            inputList.addAll(computeNumericNormResult(modelConfig, cutoff, config, val));
                        }
                    }
                } else {
                    if(!config.isMeta() && !config.isTarget() && CommonUtils.isGoodCandidate(config, hasCandidates)) {
                        String val = getNSVariableVal(rawNsDataMap, key);
                        if(CommonUtils.isTreeModel(alg) && config.isCategorical()) {
                            Integer index = binCategoryMap.get(config.getColumnNum()).get(val == null ? "" : val);
                            if(index == null) {
                                // not in binCategories, should be missing value
                                // -1 as missing value
                                inputList.add(-1d);
                            } else {
                                inputList.add(index * 1d);
                            }
                        } else {
                            inputList.addAll(computeNumericNormResult(modelConfig, cutoff, config, val));
                        }
                    }
                }
            }
        }

        // god, Double [] cannot be casted to double[], toArray doesn't work
        int size = inputList.size();
        double[] input = new double[size];
        for(int i = 0; i < size; i++) {
            input[i] = inputList.get(i);
        }

        return new BasicMLDataPair(new BasicMLData(input), new BasicMLData(ideal));
    }

    /**
     * Simple name without name space part. For segment expansion, only retain raw column name but not current column
     * name.
     *
     * @param columnConfig      the column configuration
     * @param columnConfigList  the column config list inculding all segment expansion columns if have
     * @param segmentExpansions segment expansion expressions
     * @param dataSetHeaders    data set headers for all raw columns
     * @return the simple name not including name space part
     */
    public static String getSimpleColumnName(ColumnConfig columnConfig, List<ColumnConfig> columnConfigList,
            List<String> segmentExpansions, String[] dataSetHeaders) {
        if(segmentExpansions == null || segmentExpansions.size() == 0) {
            return getSimpleColumnName(columnConfig.getColumnName());
        }

        // if(columnConfigList.size() != dataSetHeaders.size() * (segmentExpansions.size() + 1)) {
        // throw new IllegalStateException(
        // "Segment expansion enabled but # of columns in ColumnConfig.json is not consistent with segment expansion
        // files.");
        // }

        if(columnConfig.getColumnNum() >= dataSetHeaders.length) {
            return getSimpleColumnName(
                    columnConfigList.get(columnConfig.getColumnNum() % dataSetHeaders.length).getColumnName());
        } else {
            return getSimpleColumnName(columnConfig.getColumnName());
        }
    }

    /**
     * Get column name without namespace
     *
     * @param columnName - full column name
     * @return column name without namespace
     */
    public static String getSimpleColumnName(String columnName) {
        // remove name-space in column name to make it be called by simple name
        if(columnName.contains(CommonConstants.NAMESPACE_DELIMITER)) {
            columnName = columnName.substring(
                    columnName.lastIndexOf(CommonConstants.NAMESPACE_DELIMITER) + CommonConstants.NAMESPACE_DELIMITER
                            .length(), columnName.length());
        }
        return columnName;
    }

    /**
     * Assemble map data to Encog standard input format. If no variable selected(noVarSel = true), all candidate
     * variables will be selected.
     *
     * @param binCategoryMap   categorical map
     * @param noVarSel         if after var select
     * @param modelConfig      model config instance
     * @param columnConfigList column config list
     * @param rawNsDataMap     raw NSColumn data
     * @param cutoff           cut off value
     * @param alg              algorithm used in model
     * @param featureSet       feature set used in NN model
     * @return data pair instance
     * @throws NullPointerException  if input is null
     * @throws NumberFormatException if column value is not number format.
     */
    public static MLDataPair assembleNsDataPair(Map<Integer, Map<String, Integer>> binCategoryMap, boolean noVarSel,
            ModelConfig modelConfig, List<ColumnConfig> columnConfigList, Map<NSColumn, String> rawNsDataMap,
            double cutoff, String alg, Set<Integer> featureSet) {
        if(CollectionUtils.isEmpty(featureSet)) {
            return assembleNsDataPair(binCategoryMap, noVarSel, modelConfig, columnConfigList, rawNsDataMap, cutoff,
                    alg);
        }
        double[] ideal = { Constants.DEFAULT_IDEAL_VALUE };

        List<Double> inputList = new ArrayList<Double>();
        for(ColumnConfig config : columnConfigList) {
            if(config == null) {
                continue;
            }
            NSColumn key = new NSColumn(config.getColumnName());
            if(config.isFinalSelect() // check whole name
                    && !rawNsDataMap.containsKey(key) // and then check simple name, in case user use wrong namespace
                    && !rawNsDataMap.containsKey(new NSColumn(key.getSimpleName()))) {
                throw new IllegalStateException(String.format("Variable Missing in Test Data: %s", key));
            }

            if(config.isTarget()) {
                continue;
            } else {
                if(featureSet.contains(config.getColumnNum())) {
                    String val = getNSVariableVal(rawNsDataMap, key);
                    if(CommonUtils.isTreeModel(alg) && config.isCategorical()) {
                        Integer index = binCategoryMap.get(config.getColumnNum()).get(val == null ? "" : val);
                        if(index == null) {
                            // not in binCategories, should be missing value -1 as missing value
                            inputList.add(-1d);
                        } else {
                            inputList.add(index * 1d);
                        }
                    } else {
                        inputList.addAll(computeNumericNormResult(modelConfig, cutoff, config, val));
                    }
                }
            }
        }

        // god, Double [] cannot be casted to double[], toArray doesn't work
        int size = inputList.size();
        double[] input = new double[size];
        for(int i = 0; i < size; i++) {
            input[i] = inputList.get(i);
        }

        return new BasicMLDataPair(new BasicMLData(input), new BasicMLData(ideal));
    }

    /**
     * Get all available feature ids from ColumnConfig list.
     * There are two situations for this:
     * 1) when training model, get all available features before start
     * 2) get all available features before doing variable selection
     *
     * @param columnConfigList - ColumnConfig list to check
     * @param isAfterVarSelect - true for training, false for variable selection
     * @return - available feature list
     */
    public static List<Integer> getAllFeatureList(List<ColumnConfig> columnConfigList, boolean isAfterVarSelect) {
        boolean hasCandidate = CommonUtils.hasCandidateColumns(columnConfigList);

        List<Integer> features = new ArrayList<Integer>();
        List<String> wrongFeatures = new ArrayList<String>();
        for(ColumnConfig config : columnConfigList) {
            if(isAfterVarSelect) {
                if(config.isFinalSelect() && !config.isTarget() && !config.isMeta()) {
                    // only select numerical feature with getBinBoundary().size() larger than 1
                    // or categorical feature with getBinCategory().size() larger than 0
                    if((config.isNumerical() && config.getBinBoundary() != null && config.getBinBoundary().size() > 0)
                            || (config.isCategorical() && config.getBinCategory() != null
                            && config.getBinCategory().size() > 0)) {
                        features.add(config.getColumnNum());
                    } else if((config.isNumerical() && (config.getBinBoundary() == null
                            || config.getBinBoundary().size() <= 0)) || (config.isCategorical() && (
                            config.getBinCategory() == null || config.getBinCategory().size() <= 0))) {
                        wrongFeatures.add(config.getColumnName());
                    }
                }
            } else {
                if(!config.isMeta() && !config.isTarget() && CommonUtils.isGoodCandidate(config, hasCandidate)) {
                    // only select numerical feature with getBinBoundary().size() larger than 1
                    // or categorical feature with getBinCategory().size() larger than 0
                    if((config.isNumerical() && config.getBinBoundary() != null && config.getBinBoundary().size() > 0)
                            || (config.isCategorical() && config.getBinCategory() != null
                            && config.getBinCategory().size() > 0)) {
                        features.add(config.getColumnNum());
                    } else if((config.isNumerical() && (config.getBinBoundary() == null
                            || config.getBinBoundary().size() <= 0)) || (config.isCategorical() && (
                            config.getBinCategory() == null || config.getBinCategory().size() <= 0))) {
                        wrongFeatures.add(config.getColumnName());
                    }
                }
            }
        }

        if(!wrongFeatures.isEmpty()) {
            throw new IllegalStateException(
                    "Some columns config should not be selected due to bin issue: " + wrongFeatures.toString());
        }

        return features;
    }

    /**
     * Convert (String, String) raw data map to (NSColumn, String) data map
     *
     * @param rawDataMap - (String, String) raw data map
     * @return (NSColumn, String) data map
     */
    public static Map<NSColumn, String> convertRawMapToNsDataMap(Map<String, String> rawDataMap) {
        if(rawDataMap == null) {
            return null;
        }

        Map<NSColumn, String> nsDataMap = new HashMap<NSColumn, String>();
        for(String key : rawDataMap.keySet()) {
            nsDataMap.put(new NSColumn(key), rawDataMap.get(key));
        }
        return nsDataMap;
    }

    /**
     * Normalize variable by (modelType, normMethod). One variable val could be normalized into multi double value
     *
     * @param modelConfig - to check modelType. TreeModel or NN/LR model?
     * @param cutoff      - cutoff for ZScale
     * @param config      - variable configuration
     * @param val         - raw variable value
     * @return - most normalization method return 1 element double list,
     * but OneHot will will return multi-elements double list
     */
    private static List<Double> computeNumericNormResult(ModelConfig modelConfig, double cutoff, ColumnConfig config,
            String val) {
        List<Double> normalizeValue = null;
        if(CommonUtils.isTreeModel(modelConfig.getAlgorithm())) {
            try {
                normalizeValue = Arrays.asList(new Double[] { Double.parseDouble(val) });
            } catch (Exception e) {
                normalizeValue = Arrays.asList(new Double[] { Normalizer.defaultMissingValue(config) });
            }
        } else {
            normalizeValue = Normalizer.normalize(config, val, cutoff, modelConfig.getNormalizeType());
        }

        if(CollectionUtils.isNotEmpty(normalizeValue)) {
            for(int i = 0; i < normalizeValue.size(); i++) {
                Double nval = normalizeValue.get(i);
                if(Double.isInfinite(nval) || Double.isNaN(nval)) {
                    // if the value is Infinite or NaN, treat it as missing value
                    // should treat Infinite as missing value also?
                    normalizeValue.set(i, defaultMissingValue(config));
                }
            }
        }
        return normalizeValue;
    }

    /**
     * Get data from raw data map (with {@link NSColumn} as key)
     *
     * @param rawNsDataMap raw data map to look up
     * @param key - {@link NSColumn} for variable
     * @return raw value map
     */
    public static String getNSVariableVal(Map<NSColumn, String> rawNsDataMap, NSColumn key) {
        String val = rawNsDataMap.get(key);
        return (val == null ? rawNsDataMap.get(new NSColumn(key.getSimpleName())) : val);
    }

    /**
     * Get missing value of ColumnConfig. Now 'mean' value is used as missing value.
     * If mean is null, use 0.0
     *
     * @param config {@link ColumnConfig}
     * @return - default missing value of ColumnConfig
     */
    public static double defaultMissingValue(ColumnConfig config) {
        // TODO return 0 when mean == null. Is it correct or reasonable?
        return config.getMean() == null ? 0 : config.getMean().doubleValue();
    }
}
