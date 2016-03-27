/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudera.spark.mllib

import com.cloudera.spark.dataset.DatasetTitanic
import com.cloudera.spark.randomforest.JavaRandomForest
import org.apache.spark.SparkConf
import org.apache.spark.api.java.{JavaRDD, JavaSparkContext}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.{DataFrame, SQLContext}

/**
 * Created by jayantshekhar
 */
object Titanic {

  def main(args: Array[String]) {
    if (args.length < 1) {
      System.err.println("Usage: Titanic <input_file>")
      System.exit(1)
    }

    val inputFile: String = args(0)
    val sparkConf: SparkConf = new SparkConf().setAppName("JavaTitanic")
    SparkConfUtil.setConf(sparkConf)

    val sc: JavaSparkContext = new JavaSparkContext(sparkConf)
    val sqlContext: SQLContext = new SQLContext(sc)
    val results: DataFrame = DatasetTitanic.createDF(sqlContext, inputFile)

    results.printSchema

    val data: JavaRDD[LabeledPoint] = DatasetTitanic.createLabeledPointsRDD(sc, sqlContext, inputFile)
    val splits: Array[JavaRDD[LabeledPoint]] = data.randomSplit(Array[Double](0.7, 0.3))
    val trainingData: JavaRDD[LabeledPoint] = splits(0)
    val testData: JavaRDD[LabeledPoint] = splits(1)

    val categoricalFeaturesInfo: java.util.HashMap[Integer, Integer] = new java.util.HashMap[Integer, Integer]
    categoricalFeaturesInfo.put(0, 2) // feature 0 is binary (taking values 0 or 1)

    System.out.println("\nRunning classification using RandomForest\n")
    JavaRandomForest.classifyAndTest(trainingData, testData, categoricalFeaturesInfo)

    System.out.println("\nRunning example of regression using RandomForest\n")
    JavaRandomForest.testRegression(trainingData, testData)

    sc.stop
  }
}

