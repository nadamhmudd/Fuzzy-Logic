package com.company;

import java.util.*;

class subSet{
    String name;
    int type;
    float[] values;
    float centroid;
    //Vector values;

    subSet(String name, int type, float[] set){
        this.name= name;
        this.type= type;
        values= new float[type];
        for(int i=0; i<type; i++)
            values[i]= set[i];
    }
}

public class fuzzy {

    HashMap<String, Vector<subSet>> fSets= new HashMap<>();
    HashMap<String, Vector<subSet>> fSetOutput= new HashMap<>();
    Vector<String>fRules= new Vector();

    public void addFuzzySet(String fsetName,  Vector<subSet> sub, String type){//type; input or output
        if(type.equals("output"))
            fSetOutput.put(fsetName, sub);
        else
            fSets.put(fsetName, sub);
    }

    public void addRules(String rule){
        fRules.add(rule);
    }

    public void Run(Vector<String>varName, Vector<Float>varCrispValue){

        //step#1: Fuzzifying the inputs
        System.out.println("\n1- Fuzzification:");

        Vector<Vector<Float>>memshipValues= new Vector();
        for (int i=0; i<varName.size(); i++){
           if(fSets.containsKey(varName.get(i)) ){ //check name of fuzzy set
               Vector<subSet>sets= fSets.get(varName.get(i)); //get its subsets
               float crisp= varCrispValue.get(i);
               Vector<Float> temp= new Vector<>();

               System.out.println("When "+varName.get(i)+"= "+crisp+":");
               for (int j=0; j<sets.size(); j++){ //loop on each set
                   float membership=0;

                   Vector<Float> currSet=new Vector<>();
                   for (int d=0; d<sets.get(j).values.length; d++)
                       currSet.add(sets.get(j).values[d]);

                   if(currSet.contains(crisp)){// 0 or 1
                       if(currSet.firstElement()==crisp || currSet.lastElement()==crisp)
                           membership=0;
                       else
                           membership=1;

                   }
                   else if(currSet.firstElement()<crisp && currSet.lastElement()>crisp){ //calculate by Lines Equations
                       //select which line??
                       for (int x=0; x<currSet.size(); x++){
                           if(currSet.get(x)<crisp && currSet.get(x+1)>crisp) { //intersection exist with this line
                               if(x==0)
                                   membership=lineEquation(crisp, new float[]{currSet.get(x),0} , new float[]{currSet.get(x+1),1});
                               else if((x+1)== currSet.size()-1)
                                   membership=lineEquation(crisp, new float[]{currSet.get(x),1} , new float[]{currSet.get(x+1),0});
                               else
                                   membership=lineEquation(crisp, new float[]{currSet.get(x),1} , new float[]{currSet.get(x+1),1});
                               break;
                           }
                       }
                   }
                   else{//no intersection
                       membership=0;
                   }

                   temp.add(membership);
                   //print membership values
                   System.out.println(" μ."+sets.get(j).name+"= "+ membership);
               }
               memshipValues.add(temp);
           }
           System.out.println();
        }

        //step#2: Inference of rules , and->min, or->max, not->1-x
        System.out.println("2- Rules Evaluation");

        Vector<String>rulesOutput= new Vector();
        for (int i=0; i<fRules.size(); i++){
            //re write rule with membership value and operations and add it in vector rule
            Vector<String> rule=new Vector<>();
            String[] r= fRules.get(i).split(" ");
            for (int d=0; d<r.length-1; d++){ //replace text by its membership value, -1 to skip output
                if(r[d].contains("=")){
                    String[] x=r[d].split("=");
                    int subset_idx=-1;
                    Vector<subSet> vec=fSets.get(x[0]); //get subSets of this fuzzy set
                    for (int v=0; v<vec.size(); v++){
                        subSet s= vec.get(v);
                        if(s.name.equals(x[1])){
                            subset_idx= v;
                            break;
                        }
                    }
                    float memValue= memshipValues.get(varName.indexOf(x[0])).get(subset_idx);
                    rule.add(String.valueOf(memValue));
                }
                else if(r[d].equals("->")){ //output
                    String[] x=r[d+1].split("=");
                    rule.add(r[d]);
                    rule.add(x[1]);
                    break; //end of for loop
                }
                else
                    rule.add(r[d]); //add logic operation
            }

            //print output
            String str="";
            for(int d=0; d<rule.size()-2; d++) str=str+rule.get(d)+" ";
            System.out.print("Rule-"+(i+1)+": "+str+"= ");

            //calculate
            while(rule.contains("NOT") || rule.contains("AND") || rule.contains("OR")) {
                if (rule.contains("NOT")) {
                    int idx = rule.indexOf("NOT");
                    rule.set(idx + 1, String.valueOf(1 - Float.parseFloat(rule.get(idx + 1))));
                    rule.remove(idx);
                }
                if (rule.contains("AND")) {
                    int idx = rule.indexOf("AND");
                    float min = Math.min(Float.parseFloat(rule.get(idx - 1)), Float.parseFloat(rule.get(idx + 1)));
                    rule.set(idx, String.valueOf(min));
                    rule.remove(idx - 1);
                    rule.remove(idx);
                }
                if (rule.contains("OR")) {
                    int idx = rule.indexOf("OR");
                    float max = Math.max(Float.parseFloat(rule.get(idx - 1)), Float.parseFloat(rule.get(idx + 1)));
                    rule.set(idx, String.valueOf(max));
                    rule.remove(idx - 1);
                    rule.remove(idx);
                }
            } //rule.size must be equal 3 -> [value, ->, output of risk)
            rulesOutput.add(rule.get(0)+" "+rule.get(2)); //skip ->
            System.out.println(rule.get(0)+" "+rule.get(2));
        }
        //System.out.println(rulesOutput);

        //step#3: Defuzzication-> using weighted average method
        System.out.println("\n3- Defuzzication");
        //calculate centroid foreach set of outputFuzzySet
        String fuzzyOutputName="";
        for (Map.Entry<String, Vector<subSet> > idx : fSetOutput.entrySet()) { //contain 1 element
            fuzzyOutputName= idx.getKey();
            for (int i=0; i< idx.getValue().size(); i++){
                float sum= 0;
                for(int j=0; j<idx.getValue().get(i).type; j++) {
                    sum+= idx.getValue().get(i).values[j];
                }
                idx.getValue().get(i).centroid= sum/idx.getValue().get(i).type;
            }
        }

        //apply the method
        float predictedValue, sum=0, denominator=0;
        String existsIn="";

        //calculate predict value
        for (int i=0; i<rulesOutput.size(); i++){
            String[] value= rulesOutput.get(i).split(" ");
            denominator+= Float.parseFloat(value[0]);

            //get centroid of value[1];
            float centroid=0;
            for(int j=0; j< fSetOutput.get(fuzzyOutputName).size(); j++ ) {
                if(fSetOutput.get(fuzzyOutputName).get(j).name.equals(value[1])) {
                    centroid = fSetOutput.get(fuzzyOutputName).get(j).centroid;
                    break;
                }
            }
            sum= sum+ (Float.parseFloat(value[0])*centroid);
        }
        predictedValue= sum/ denominator;
        System.out.println("Predicted Value ("+fuzzyOutputName+") = "+predictedValue);

        //get Range in which predicted risk value exists
        for (Map.Entry<String, Vector<subSet> > idx : fSetOutput.entrySet()) {
            for (int i=0; i< idx.getValue().size(); i++){
                int lastindx= (idx.getValue().get(i).type) -1;
                if(idx.getValue().get(i).values[0]<= predictedValue && idx.getValue().get(i).values[lastindx]>= predictedValue) {
                    if(existsIn.equals(""))
                        existsIn = idx.getValue().get(i).name;
                    else
                        existsIn = existsIn +" OR " + idx.getValue().get(i).name;
                }
            }
        }
        System.out.println(fuzzyOutputName+" will be "+ existsIn);

    }

    private float lineEquation(float x,float[] point1 , float[] point2){ //y= ax+b, a=slope, b=y-ax
        float y=0, slope, b;

        slope= (point2[1]-point1[1])/(point2[0]-point1[0]);
        b= point1[1]-(slope*point1[0]);
        y= (slope*x)+b;

        return y;
    }







}
