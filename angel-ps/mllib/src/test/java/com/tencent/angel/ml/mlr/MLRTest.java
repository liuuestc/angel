/*
 * Tencent is pleased to support the open source community by making Angel available.
 *
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.tencent.angel.ml.mlr;

import com.tencent.angel.conf.AngelConf;
import com.tencent.angel.ml.classification.mlr.MLRRunner;
import com.tencent.angel.ml.conf.MLConf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.lib.input.CombineTextInputFormat;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;


public class MLRTest {
  private Configuration conf = new Configuration();
  private static final Log LOG = LogFactory.getLog(MLRTest.class);
  private static String LOCAL_FS = FileSystem.DEFAULT_FS;
  private static String TMP_PATH = System.getProperty("java.io.tmpdir", "/tmp");

  static {
    PropertyConfigurator.configure("../conf/log4j.properties");
  }

  /**
   * set parameter values of conf
   */
  @Before public void setConf() throws Exception {
    try {
      // Feature number of train data
      int featureNum = 124;
      // Total iteration number
      int epochNum = 10;
      // Validation sample Ratio
      double vRatio = 0.1;
      // Data format, libsvm or dummy
      String dataFmt = "libsvm";
      // Train batch number per epoch.
      double spRatio = 1.0;
      // Batch number
      int batchNum = 10;

      // Learning rate
      double learnRate = 1.0;
      // Decay of learning rate
      double decay = 0.1;
      // Regularization coefficient
      double reg = 0.02;
      int rank = 5;
      double vInit = 0.1;

      // Set local deploy mode
      conf.set(AngelConf.ANGEL_DEPLOY_MODE, "LOCAL");

      // Set basic configuration keys
      conf.setBoolean("mapred.mapper.new-api", true);
      conf.set(AngelConf.ANGEL_INPUTFORMAT_CLASS, CombineTextInputFormat.class.getName());
      conf.setBoolean(AngelConf.ANGEL_JOB_OUTPUT_PATH_DELETEONEXIST, true);

      // Set data format
      conf.set(MLConf.ML_DATA_FORMAT(), dataFmt);

      //set angel resource parameters #worker, #task, #PS
      conf.setInt(AngelConf.ANGEL_WORKERGROUP_NUMBER, 1);
      conf.setInt(AngelConf.ANGEL_WORKER_TASK_NUMBER, 1);
      conf.setInt(AngelConf.ANGEL_PS_NUMBER, 1);

      //set MLR algorithm parameters #feature #epoch
      conf.set(MLConf.ML_FEATURE_NUM(), String.valueOf(featureNum));
      conf.set(MLConf.ML_EPOCH_NUM(), String.valueOf(epochNum));
      conf.set(MLConf.ML_BATCH_SAMPLE_Ratio(), String.valueOf(spRatio));
      conf.set(MLConf.ML_VALIDATE_RATIO(), String.valueOf(vRatio));
      conf.set(MLConf.ML_LEARN_RATE(), String.valueOf(learnRate));
      conf.set(MLConf.ML_LEARN_DECAY(), String.valueOf(decay));
      conf.set(MLConf.ML_REG_L2(), String.valueOf(reg));
      conf.set(MLConf.ML_MLR_RANK(), String.valueOf(rank));
      conf.set(MLConf.ML_MLR_V_INIT(), String.valueOf(vInit));
      conf.set(MLConf.ML_SGD_BATCH_NUM(), String.valueOf(batchNum));
    } catch (Exception x) {
      LOG.error("setup failed ", x);
      throw x;
    }
  }

  @Test public void testMLR() throws Exception {
    trainOnLocalClusterTest();
    incTrainTest();
    predictTest();
  }

  private void trainOnLocalClusterTest() throws Exception {
    try {
      String inputPath = "./src/test/data/lr/a9a.train";
      String savePath = LOCAL_FS + TMP_PATH + "/model";
      String logPath = LOCAL_FS + TMP_PATH + "/MLRlog";

      // Set trainning data path
      conf.set(AngelConf.ANGEL_TRAIN_DATA_PATH, inputPath);
      // Set save model path
      conf.set(AngelConf.ANGEL_SAVE_MODEL_PATH, savePath);
      // Set log path
      conf.set(AngelConf.ANGEL_LOG_PATH, logPath);
      // Set actionType train
      conf.set(AngelConf.ANGEL_ACTION_TYPE, MLConf.ANGEL_ML_TRAIN());

      MLRRunner runner = new MLRRunner();
      runner.train(conf);
    } catch (Exception x) {
      LOG.error("run trainOnLocalClusterTest failed ", x);
      throw x;
    }
  }

  private void incTrainTest() throws Exception {
    try {
      String inputPath = "./src/test/data/lr/a9a.train";
      String loadPath = LOCAL_FS + TMP_PATH + "/model";
      String savePath = LOCAL_FS + TMP_PATH + "/newmodel";
      String logPath = LOCAL_FS + TMP_PATH + "/MLRlog";

      // Set trainning data path
      conf.set(AngelConf.ANGEL_TRAIN_DATA_PATH, inputPath);
      // Set load model path
      conf.set(AngelConf.ANGEL_LOAD_MODEL_PATH, loadPath);
      // Set save model path
      conf.set(AngelConf.ANGEL_SAVE_MODEL_PATH, savePath);
      // Set log path
      conf.set(AngelConf.ANGEL_LOG_PATH, logPath);
      // Set actionType incremental train
      conf.set(AngelConf.ANGEL_ACTION_TYPE, MLConf.ANGEL_ML_INC_TRAIN());

      MLRRunner runner = new MLRRunner();
      runner.incTrain(conf);
    } catch (Exception x) {
      LOG.error("run incTrainTest failed ", x);
      throw x;
    }
  }

  private void predictTest() throws Exception {
    try {
      String inputPath = "./src/test/data/lr/a9a.test";
      String loadPath = LOCAL_FS + TMP_PATH + "/model";
      String predictPath = LOCAL_FS + TMP_PATH + "/predict";

      // Set trainning data path
      conf.set(AngelConf.ANGEL_PREDICT_DATA_PATH, inputPath);
      // Set load model path
      conf.set(AngelConf.ANGEL_LOAD_MODEL_PATH, loadPath);
      // Set predict result path
      conf.set(AngelConf.ANGEL_PREDICT_PATH, predictPath);
      // Set actionType prediction
      conf.set(AngelConf.ANGEL_ACTION_TYPE, MLConf.ANGEL_ML_INC_TRAIN());

      conf.set(AngelConf.ANGEL_ACTION_TYPE, MLConf.ANGEL_ML_PREDICT());
      MLRRunner runner = new MLRRunner();

      runner.predict(conf);
    } catch (Exception x) {
      LOG.error("run predictTest failed ", x);
      throw x;
    }
  }
}

