/*
 * Copyright [2012-2014] PayPal Software Foundation
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
package ml.shifu.shifu.core.processor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ml.shifu.shifu.container.obj.ModelBasicConf.RunMode;
import ml.shifu.shifu.container.obj.ModelConfig;
import ml.shifu.shifu.container.obj.ModelTrainConf.ALGORITHM;
import ml.shifu.shifu.exception.ShifuException;
import ml.shifu.shifu.util.GSUtils;
import ml.shifu.shifu.util.HDFSUtils;
import ml.shifu.shifu.util.JSONUtils;

/**
 * CreateModelProcessor class
 */
public class CreateModelProcessor extends BasicModelProcessor implements Processor {

    /**
     * log object
     */
    private final static Logger log = LoggerFactory.getLogger(CreateModelProcessor.class);

    /**
     * model name and description
     */
    private String name;
    private ALGORITHM alg;
    private String description;

    /**
     * Constructor, giving a model and a model description to initialize a model configuration
     * 
     * @param modelSetName
     *            the model set name
     * @param modelAlg
     *            the algorithm
     * @param description
     *            the description
     */
    public CreateModelProcessor(String modelSetName, ALGORITHM modelAlg, String description) {
        this.name = modelSetName;
        this.alg = modelAlg;
        this.description = description;
    }

    /**
     * Runner, running the create model processor
     * 
     * @throws IOException
     *             - when creating files
     */
    @Override
    public int run() throws IOException {
        File modelSetFolder = new File(name);
        if(modelSetFolder.exists()) {
            log.error("ModelSet - {} already exists.", name);
            return 1;
        }

        try {
            log.info("Creating ModelSet Folder: " + modelSetFolder.getCanonicalPath() + "...");
            FileUtils.forceMkdir(modelSetFolder);

            log.info("Creating Initial ModelConfig.json ...");

            // how to check hdfs
            boolean enableHadoop = HDFSUtils.isDistributedMode();
            // check google cloud environment
            boolean enableGcp = GSUtils.isGoogleCloudEnvironment();
            RunMode runMode = RunMode.LOCAL;
            if(enableHadoop) {
                log.info("Enable DIST/MAPRED mode because Hadoop cluster is detected.");
                runMode = RunMode.MAPRED;
            } else if (enableGcp){
                log.info("Enable GCP mode because google cloud configuration is detected.");
                runMode = RunMode.GCP;
            } else{
                log.info("Enable LOCAL mode because neither Hadoop cluster or google cloud environment is detected.");
                runMode = RunMode.LOCAL;
            }

            modelConfig = ModelConfig.createInitModelConfig(name, alg, description, runMode);

            JSONUtils.writeValue(new File(modelSetFolder.getCanonicalPath() + File.separator + "ModelConfig.json"),
                    modelConfig);

            createHead(modelSetFolder.getCanonicalPath());

            log.info("Step Finished: new");
        } catch (ShifuException e) {
            log.error("Error:" + e.getError().toString() + "; msg:" + e.getMessage(), e);
            return -1;
        } catch (Exception e) {
            log.error("Error:" + e.getMessage(), e);
            return -1;
        }
        return 0;
    }

}
