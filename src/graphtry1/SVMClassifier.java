/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtry1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import liblinear.FeatureNode;
import liblinear.Linear;
import liblinear.Model;
import liblinear.Parameter;
import liblinear.Problem;
import liblinear.SolverType;

public class SVMClassifier {

  private static final String dataDir = "data";
  private static final String trainDir = "newdata";
  //private static final String featureFile = "features-short.txt";
  private static final String featureFile = "features.txt";
  //private static final String modelSuffix = "-short-model";
  private static final String modelSuffix = "-demo-model";
  // maximum n-gram length
  private static final int maxN = 1;

  private HashMap<String, Integer> featureMap;
  private Model[] models;
    private String[] emotions2;
    private String installDir2;

  public SVMClassifier(String installDir, String[] emotions) {
    models = new Model[emotions.length];
    emotions2= emotions;
    installDir2 = installDir;
    
    try {
      readFeatureMap(installDir + File.separator + dataDir + File.separator + featureFile);
      for (int i=0; i<models.length; i++) {
        models[i] = Linear.loadModel(new File(installDir + File.separator + dataDir + File.separator + emotions[i] + modelSuffix));
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  // predict labels for a string
  public double[] predict(String sentence) {
    // extract features from string
    FeatureNode[] sample = getFeatures(sentence, false);
    // get label per classifier model
    double[] val = new double[2];
    double[] res = new double[models.length];
    for (int i=0; i<models.length; i++) {
      int r = Linear.predictValues(models[i], sample, val);
      System.out.println(i + " " + emotions2[i] + " res " + r + " raw " + Arrays.toString(val));
//      res[i] = val[0]; // raw values
      res[i] = val[0]/(1.0+abs((float)val[0])); // [-1, +1]-scaled values
    }
    System.out.println("predictions " + Arrays.toString(res));
    return res;
  }


  // get n-grams from array of words
  private List<String> getNGrams(String[] words) {
    ArrayList<String> w = new ArrayList<String>(Arrays.asList(words));
    w.add(0, "__START__");
    w.add("__END__");
    List<String> ngrams = new ArrayList<String>();      
    int maxNgramSize = Math.min(w.size(), maxN);
    for (int ngramSize = 1; ngramSize <= maxNgramSize; ngramSize++) {
      for (int wordIndex = 0; wordIndex < w.size() - ngramSize + 1; wordIndex++) {
        String ngram = "";
        for (int i = 0; i < ngramSize; i++) {
          ngram = ngram + " " + w.get(wordIndex+i);
        }
        ngrams.add(ngram.trim());
      }
    }
    return ngrams;
  }

  // get feature vector from string
  private FeatureNode[] getFeatures(String s, boolean addNew) {
    String[] words = Utils.preProcess(s);
    List<String> ngrams = getNGrams(words);
//    println("feature vector " + ngrams);
    HashMap<Integer, Double> featMap = new HashMap<Integer, Double>();
    for (String feature : ngrams) {
      Integer i = featureMap.get(feature);
      if (addNew && (i == null)) {
        i = featureMap.size() + 1;
        featureMap.put(feature, i);
      }
      if (i != null) {
        if (!featMap.containsKey(i)) {
          featMap.put(i, new Double(0.0));
        }
        featMap.put(i, new Double(featMap.get(i) + 1.0));
      }
    }
    // normalize, sort
    double sum = 0.0;
    for (Integer i : featMap.keySet()) {
      sum += featMap.get(i);
    }
    ArrayList<Integer> indices = new ArrayList<Integer>(featMap.keySet());
    Collections.sort(indices);
    FeatureNode[] fn = new FeatureNode[indices.size()];
    for (int i = 0; i < fn.length; i++) {
      fn[i] = new FeatureNode(indices.get(i), featMap.get(indices.get(i)) / sum);
//      System.out.println("fn " + i + "=" + fn[i]);
    }
    return fn;
  }
  

  // read in a map from features to indices
  private void readFeatureMap(String fileName) {
      featureMap = new HashMap<String, Integer>();
      for (String line : Utils.readFileLines(fileName)) {
        String[] f = line.split("\\t");
        featureMap.put(f[1].trim(), new Integer(f[0].trim()));
      }
  }
  
  // write a map from features to indices 
  private void writeFeatureMap(String fileName) {
    ArrayList<String> lines = new ArrayList<String>();
    try {
      FileOutputStream outstream = new FileOutputStream(new File(fileName));
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outstream));
      HashMap<Integer, String> reverseMap = new HashMap<Integer, String>(); 
      for (String f : featureMap.keySet()) {
        reverseMap.put(featureMap.get(f), f);
      }
      List<Integer> indices = new ArrayList<Integer>(reverseMap.keySet());
      Collections.sort(indices);
      for (Integer i : indices) {
        writer.write(i + "\t" + reverseMap.get(i) + "\n");
      }
      writer.close();        
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }
  
  // train new models
  public int train() {
    Model[] newmodels = new Model[emotions2.length];
//    HashMap<String, ArrayList<Integer>> labelMap = new HashMap<String, ArrayList<Integer>>();
//    for (String emotion : emotions) {
//      labelMap.put(emotion, new ArrayList<Integer>());
//    }
    ArrayList<FeatureNode[]> instances = new ArrayList<FeatureNode[]>();
    ArrayList<String> emoLabels = new ArrayList<String>();
    featureMap.clear(); 
    for (String emotion : emotions2) {
      List<String> lines = Utils.readFileLines(installDir2 + File.separator + trainDir + File.separator + emotion + ".txt");
      for (String line : lines) {
        System.out.println(line);
        FeatureNode[] fn = getFeatures(line, true);
        instances.add(fn);
        System.out.println(Arrays.toString(fn));
        emoLabels.add(emotion);
//        for (String emotion2 : emotions) {
//          labelMap.get(emotion2).add(emotion2.equals(emotion) ? new Integer(1) : new Integer(-1));
//        }
      }
    }
    writeFeatureMap(installDir2 + File.separator + trainDir + File.separator + featureFile);
//    FeatureNode[][] x = instances.toArray(new FeatureNode[instances.size()][]);
    FeatureNode[][] x = new FeatureNode[instances.size()][];
    int[] y = new int[instances.size()];
    // we must arrange data in same (pos; neg) label order, so all models have same label order
    for (int iE=0; iE<models.length; iE++) {
      String emotion = emotions2[iE];
      Problem problem = new Problem();
      problem.bias = -1;
      problem.l = instances.size(); // int number of training examples
      problem.n = featureMap.size(); // int number of features
      // compile list of training instances, positive then negative
      int iX = 0;
      int n_pos = 0;
      for (int i=0; i<instances.size(); i++) {
        if (emoLabels.get(i).equals(emotion)) {
          x[iX] = instances.get(i); 
          y[iX] = 1;
          n_pos++;
          iX++;
        } 
      }
      int n_neg = 0;
      for (int i=0; i<instances.size(); i++) {
        if (!emoLabels.get(i).equals(emotion)) {
          x[iX] = instances.get(i); 
          y[iX] = -1;
          n_neg++;
          iX++;
        } 
      }
      problem.x = x; // FeatureNode[] feature nodes
      problem.y = y; // double[] target values
      System.out.println(emotion + " problem " + problem.l + " " + problem.n + " " + Arrays.toString(problem.y));

      SolverType solver = SolverType.L2R_L2LOSS_SVC_DUAL; // -s 1
      double C = 1000.0/((double) instances.size());    // cost of constraints violation
      double eps = 0.1; // stopping criteria
      // set weight per class in case data is very unbalanced
      Parameter parameter = new Parameter(solver, C, eps);
      //parameter.setWeights(new double[]{1.0, ((double) n_neg / (double) n_pos)}, new int[]{0, 1});
      
      Model model = Linear.train(problem, parameter);
      //System.out.println("model: " + model.nr_class + " " + Arrays.toString(model.label));
//      FeatureNode[] sample = getFeatures("i hate you", false);
//      System.out.println("sample: " + Arrays.toString(sample));
//      double[] val = new double[2];
//      int r = Linear.predictValues(model, sample, val);
//      System.out.println("test: " + r + " " + Arrays.toString(val));
      try {
        model.save(new File(installDir2 + File.separator + trainDir + File.separator + emotion + "-model"));
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(0);
      }
      // if we got this far, update working models
      models[iE] = model; 
    }
    return 0;
  }
  
}