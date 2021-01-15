package ml.shifu.shifu.dataproc;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ml.shifu.guagua.util.NumberFormatUtils;
import ml.shifu.shifu.container.obj.ModelConfig;
import ml.shifu.shifu.container.obj.RawSourceData.SourceType;
import ml.shifu.shifu.core.dtrain.CommonConstants;
import ml.shifu.shifu.fs.PathFinder;
import ml.shifu.shifu.util.CommonUtils;
import ml.shifu.shifu.util.Constants;
import ml.shifu.shifu.util.Environment;
import ml.shifu.shifu.util.GSUtils;
import ml.shifu.shifu.util.GSUtils.CommandExecutionOutput;


/**
 * DataProcExecutor is used to submit map reduced job, pig job to GCP DataProc cluster
 */
public class DataProcExecutor {
    private static Logger log = LoggerFactory.getLogger(DataProcExecutor.class);
    private static DataProcExecutor instance = new DataProcExecutor();

    // avoid to create instance, used as singleton
    private DataProcExecutor() {
    }

    /**
     * Get the pig executor handler
     * 
     * @return - executor handler
     */
    public static DataProcExecutor getExecutor() {
        return instance;
    }

    /**
     * Submit the pig job with @ModelConfig and pig script
     * This functions doesn't allow customer setting
     * 
     * @param modelConfig
     *            - model configuration
     * @param pigScriptPath
     *            - path of pig script
     * @throws IOException
     *             throw IOException when loading the parameter from @ModelConfig
     */
    public void submitPigJob(ModelConfig modelConfig, String pigScriptPath) throws IOException {
        submitPigJob(modelConfig, pigScriptPath, null);
    }

    /**
     * Run the pig, Local or MapReduce mode is decide by the training source data type in modelConfig
     * 
     * @param modelConfig
     *            - model configuration
     * @param pigScriptPath
     *            - path of pig script
     * @param paramsMap
     *            - additional parameters for pig script
     * @throws IOException
     *             throw IOException when loading the parameter from @ModelConfig
     */
    public void submitPigJob(ModelConfig modelConfig, String pigScriptPath, Map<String, String> paramsMap)
            throws IOException {
                submitPigJob(modelConfig, pigScriptPath, paramsMap, modelConfig.getDataSet().getSource(), null);
    }

    public void submitPigJob(ModelConfig modelConfig, String pigScriptPath, Map<String, String> paramsMap,
            SourceType sourceType) throws IOException {
                submitPigJob(modelConfig, pigScriptPath, paramsMap, sourceType, null);
    }

    public void submitPigJob(ModelConfig modelConfig, String pigScriptPath, Map<String, String> paramsMap,
            SourceType sourceType, PathFinder pathFinder) throws IOException {
                submitPigJob(modelConfig, pigScriptPath, paramsMap, sourceType, null, pathFinder);
    }

    /**
     * Run the pig, Local or MapReduce mode is decide by parameter @sourceTpe
     * 
     * @param modelConfig
     *            - model configuration
     * @param pigScriptPath
     *            - path of pig script
     * @param paramsMap
     *            - additional parameters for pig script
     * @param sourceType
     *            - the mode run pig: pig-local/pig-hdfs
     * @param confMap
     *            the configuration map instance
     * @param pathFinder
     *            the path finder
     * @throws IOException
     *             throw IOException when loading the parameter from @ModelConfig
     */
    public void submitPigJob(ModelConfig modelConfig, String pigScriptPath, Map<String, String> paramsMap,
            SourceType sourceType, Map<String, String> confMap, PathFinder pathFinder) throws IOException{
                Map<String, String> pigParamsMap = null;
                if(modelConfig.isMultiTask()) {
                    if(paramsMap != null) {
                        int mtlIndex = NumberFormatUtils.getInt(paramsMap.get(CommonConstants.MTL_INDEX), true);
                        pigParamsMap = CommonUtils.getPigParamMap(modelConfig, sourceType, pathFinder, mtlIndex);
                    }
                } else {
                    pigParamsMap = CommonUtils.getPigParamMap(modelConfig, sourceType, pathFinder);
                }
        
                if(paramsMap != null) {
                    pigParamsMap.putAll(paramsMap);
                }
                log.debug("Pig submit parameters: {}", pigParamsMap);
                if(new File(pigScriptPath).isAbsolute()) {
                    log.info("Pig script absolute path is {}", pigScriptPath);
                    String commandTemplate = "gcloud dataproc jobs submit pig --cluster={0} --file={1} --params={2}";
                    List<String> paramsList = new ArrayList<String>();
                    String bucketName = Environment.getProperty(Environment.GCP_STORAGE_BUCKET);
                    pigParamsMap.entrySet().stream().forEach(e -> {
                        if (e.getKey() == "delimiter" || e.getKey() == "output_delimiter"){
                            paramsList.add(e.getKey() + "=\"" + e.getValue() + "\"");
                        }else if (StringUtils.startsWith(e.getKey().toLowerCase(), "path") && e.getKey() != Constants.PATH_JAR){
                            paramsList.add(e.getKey() + "=" + GSUtils.getFullyQualifiedPath(bucketName, e.getValue()));
                        }else{
                            paramsList.add(e.getKey() + "=" + e.getValue());
                        }
                    });
                    String command = MessageFormat.format(commandTemplate, "cluster-ken-testing", pigScriptPath, paramsList.stream().collect(Collectors.joining(",")));
                    try{
                        log.debug("Pig submit command: {}", command);
                        CommandExecutionOutput output =GSUtils.executeCommand(command);
                        for(String line : output.getDetails()){
                            log.info(line);
                        }
                    }catch(Exception ex){
                        throw new IOException(ex.getLocalizedMessage(), ex);
                    }
                } else {
                    log.info("Pig script relative path is {}", pigScriptPath);
                    // pigServer.registerScript(DataProcExecutor.class.getClassLoader().getResourceAsStream(pigScriptPath),
                    //         pigParamsMap);
                }
            }

}
