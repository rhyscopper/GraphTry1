/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package graphtry1;

import java.util.ArrayList;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage; 
import javafx.collections.FXCollections; 
 
public class GraphTry1 extends Application {
 
    // reads the emotions and stores them in an array
    String example ="Happy,sad,surprise";
    String[] XAxisName = example.split(","); 
    int Numberofemotions = XAxisName.length;
    
    // where are the classifier models?
    String installDir = "/Users/Rhys/NetBeansProjects/Classifier/rhys";
    
    SVMClassifier svm = new SVMClassifier(installDir, XAxisName);
	
    // if it's new, classify and start new display
      
    private ArrayList<double[]> Data = new ArrayList<double[]>();
   
    @Override public void start(Stage stage) {
        
       // if it's new, classify and start new display
	String[] sents = {"what a lovely day it is today :)", "oh man that scared the crap out of me","love you", "cannot wait for this to be over","omg i cannot believe bob died","cannot wait to learn","you all cunts","seeing is believing","yay","rip to jill","potatoe"};
	for (int s=0; s<sents.length; s++) { 
	    
	    String sent = sents[s]; 
            System.out.println("\nnew sentence: " + sent);
	    
	    // classify
	    // get raw list of confidences for each emotion
	    double[] p = svm.predict(sent);
	    // scale to [0,1] and find max
	    double max = -1.0;
	    int maxind = -1;
	    for (int i=0; i<p.length; i++) {
		p[i] = 1.0 / (1.0 + Math.exp(-p[i]));
		if (p[i]>max) {
		    maxind = i;
		    max = p[i];
		} 
                System.out.println(p[i]);
	    }
            if(Data.size() >= 10){
                
                        Data.remove(0);
                  
            }
	    Data.add(p); 
	}

        // sets the title of the program
        stage.setTitle("Emotion Analyser");
        //creates the x and y axis
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        //sets the names of the collums to a constant.
        xAxis.setCategories(FXCollections.<String>observableArrayList("1st","2nd","3rd","4th","5th","6th","7th","8th","9th","10th"));
        // sets the name on the y axis
         xAxis.setLabel("Utterances");
        //initialises the graph
        final LineChart<String,Number> lineChart = 
                new LineChart<String,Number>(xAxis,yAxis);
        //sets the title that appears on the analyser
        lineChart.setTitle("Emotion Analysis");
        
        // create anarraylist of type XYChart     
        ArrayList<XYChart.Series> list = new ArrayList<XYChart.Series>();
       
        // creates a series for the number of emotions entered
        for(int i = 0; i < Numberofemotions; i++){
            
            list.add(new XYChart.Series());
        
        } 
        
        
        int a = Data.size();
        for(int j = 0; j < Numberofemotions; j++){
            
            list.get(j).setName(XAxisName[j]); 
            
            for(int c = 0; c < a ; c++){
                double[] data = Data.get(c);
                if(c==0){
                    list.get(j).getData().add(new XYChart.Data("1st",data[j]));
                } 
                if(c==1){
                    list.get(j).getData().add(new XYChart.Data("2nd",data[j]));
                } 
                if(c==2){
                    list.get(j).getData().add(new XYChart.Data("3rd",data[j]));
                } 
                if(c==3){
                    list.get(j).getData().add(new XYChart.Data("4th",data[j]));
                } 
                if(c==4){
                    list.get(j).getData().add(new XYChart.Data("5th",data[j]));
                } 
                if(c==5){
                    list.get(j).getData().add(new XYChart.Data("6th",data[j]));
                } 
                if(c==6){
                    list.get(j).getData().add(new XYChart.Data("7th",data[j]));
                } 
                if(c==7){
                    list.get(j).getData().add(new XYChart.Data("8th",data[j]));
                } 
                if(c==8){
                    list.get(j).getData().add(new XYChart.Data("9th",data[j]));
                } 
                if(c==9){
                    list.get(j).getData().add(new XYChart.Data("10th",data[j]));
                }  
            }
        }
        
        Scene scene  = new Scene(lineChart,800,600);   
        
        for(int k = 0; k < Numberofemotions; k++){
            lineChart.getData().add(list.get(k));
        }
        
        stage.setScene(scene);
        stage.show();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}